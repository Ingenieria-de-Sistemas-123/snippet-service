package com.snippetsearcher.snippet.service;

import com.snippetsearcher.snippet.client.AssetClient;
import com.snippetsearcher.snippet.client.PermissionClient;
import com.snippetsearcher.snippet.dto.UserAccountDto;
import com.snippetsearcher.snippet.dto.request.CreateSnippetRequest;
import com.snippetsearcher.snippet.dto.request.ShareSnippetRequest;
import com.snippetsearcher.snippet.dto.request.UpdateSnippetRequest;
import com.snippetsearcher.snippet.dto.response.ListSnippetsResponse;
import com.snippetsearcher.snippet.dto.response.SnippetListItemResponse;
import com.snippetsearcher.snippet.dto.response.SnippetResponse;
import com.snippetsearcher.snippet.exception.SnippetNotFoundException;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.repository.SnippetRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SnippetService {

  private static final Logger log = LoggerFactory.getLogger(SnippetService.class);

  private final SnippetRepository snippetRepository;
  private final AssetClient assetClient;
  private final PermissionClient permissionClient;
  private final LanguageValidationService languageValidationService;
  private final String snippetsContainer;

  public SnippetService(
      SnippetRepository snippetRepository,
      AssetClient assetClient,
      PermissionClient permissionClient,
      LanguageValidationService languageValidationService,
      @Value("${asset-service.snippets-container:snippets}") String snippetsContainer) {
    this.snippetRepository = snippetRepository;
    this.assetClient = assetClient;
    this.permissionClient = permissionClient;
    this.languageValidationService = languageValidationService;
    this.snippetsContainer = snippetsContainer;
  }

  /**
   * Use Case 1: - asegura usuario en permission-service - sube archivo a asset-service - crea
   * snippet en DB - crea permiso OWNER en permission-service
   */
  @Transactional
  public SnippetResponse createSnippet(Jwt jwt, CreateSnippetRequest request, MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("El archivo del snippet es obligatorio.");
    }

    // 1) Asegurar/obtener usuario en permission-service
    UserAccountDto user = ensureUser(jwt);
    UUID userId = user.id();

    // 2) Subir archivo a asset-service
    String originalFilename =
        (file.getOriginalFilename() != null && !file.getOriginalFilename().isBlank())
            ? sanitizeFilename(file.getOriginalFilename())
            : "snippet.prs";

    String key = UUID.randomUUID() + "-" + originalFilename;

    byte[] content;
    try {
      content = file.getBytes();
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo leer el contenido del archivo del snippet.", e);
    }

    validateLanguage(request, content);

    String assetKey =
        assetClient.uploadSnippet(snippetsContainer, key, content, file.getContentType());

    // 3) Crear snippet en DB local
    Snippet snippet =
        new Snippet(request.name(), request.language(), request.description(), assetKey, userId);
    snippet = snippetRepository.save(snippet);

    // 4) Crear permiso OWNER en permission-service
    registerOwnerPermission(jwt, userId, snippet.getId());

    // 5) Devolver respuesta
    return SnippetResponse.fromEntity(snippet);
  }

  @Transactional(readOnly = true)
  public ListSnippetsResponse listSnippets(Jwt jwt, int page, int pageSize, String nameFilter) {
    UserAccountDto user = ensureUser(jwt);
    Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "updatedAt"));

    var snippetsPage =
        StringUtils.hasText(nameFilter)
            ? snippetRepository.findByOwnerUserIdAndNameContainingIgnoreCase(
                user.id(), nameFilter.trim(), pageable)
            : snippetRepository.findByOwnerUserId(user.id(), pageable);

    List<SnippetListItemResponse> items =
        snippetsPage.getContent().stream()
            .map(
                snippet ->
                    SnippetListItemResponse.fromEntity(
                        snippet, extractExtension(snippet.getAssetKey())))
            .toList();

    return new ListSnippetsResponse(
        snippetsPage.getNumber(), snippetsPage.getSize(), snippetsPage.getTotalElements(), items);
  }

  @Transactional(readOnly = true)
  public SnippetResponse getSnippet(Jwt jwt, UUID snippetId) {
    UserAccountDto user = ensureUser(jwt);
    return SnippetResponse.fromEntity(loadSnippetOwnedBy(snippetId, user.id()));
  }

  @Transactional
  public SnippetResponse updateSnippet(Jwt jwt, UUID snippetId, UpdateSnippetRequest request) {
    UserAccountDto user = ensureUser(jwt);
    Snippet snippet = loadSnippetOwnedBy(snippetId, user.id());
    snippet.setName(request.name());
    snippet.setLanguage(request.language());
    snippet.setDescription(request.description());
    snippet = snippetRepository.save(snippet);
    return SnippetResponse.fromEntity(snippet);
  }

  @Transactional
  public void deleteSnippet(Jwt jwt, UUID snippetId) {
    UserAccountDto user = ensureUser(jwt);
    Snippet snippet = loadSnippetOwnedBy(snippetId, user.id());
    snippetRepository.delete(snippet);
  }

  @Transactional
  public SnippetResponse shareSnippet(Jwt jwt, UUID snippetId, ShareSnippetRequest request) {
    if (request == null || request.userId() == null) {
      throw new IllegalArgumentException("El usuario destino es obligatorio.");
    }
    UserAccountDto user = ensureUser(jwt);
    Snippet snippet = loadSnippetOwnedBy(snippetId, user.id());

    if (request.userId().equals(snippet.getOwnerUserId())) {
      throw new IllegalArgumentException("No se puede compartir con el dueño del snippet.");
    }

    createSharedPermission(jwt, snippetId, request.userId());
    return SnippetResponse.fromEntity(snippet);
  }

  private void validateLanguage(CreateSnippetRequest request, byte[] content) {
    var validation =
        languageValidationService.validate(
            request.language(), null, new String(content, StandardCharsets.UTF_8));

    if (validation != null && !validation.valid()) {
      var firstError =
          validation.errors() != null && !validation.errors().isEmpty()
              ? validation.errors().getFirst()
              : null;

      if (firstError != null) {
        throw new IllegalArgumentException(
            "El snippet no es válido para el lenguaje %s: %s (línea %d, columna %d)."
                .formatted(
                    request.language(), firstError.message(), firstError.line(), firstError.col()));
      }

      throw new IllegalArgumentException(
          "El snippet no es válido para el lenguaje %s.".formatted(request.language()));
    }
  }

  private UserAccountDto ensureUser(Jwt jwt) {
    try {
      return permissionClient.ensureUser(jwt.getTokenValue());
    } catch (RestClientException | IllegalStateException ex) {
      log.warn("Fallo al sincronizar usuario en permission-service: {}", ex.getMessage());
      return buildUserFromJwt(jwt);
    }
  }

  private UserAccountDto buildUserFromJwt(Jwt jwt) {
    String subject = jwt.getClaimAsString("sub");
    if (!StringUtils.hasText(subject)) {
      subject = jwt.getSubject();
    }
    if (!StringUtils.hasText(subject)) {
      subject = jwt.getClaimAsString("email");
    }
    if (!StringUtils.hasText(subject)) {
      subject = "anonymous";
    }

    UUID userId = UUID.nameUUIDFromBytes(subject.getBytes(StandardCharsets.UTF_8));
    String email = jwt.getClaimAsString("email");
    String name = jwt.getClaimAsString("name");
    String picture = jwt.getClaimAsString("picture");
    return new UserAccountDto(userId, subject, email, name, picture);
  }

  private void registerOwnerPermission(Jwt jwt, UUID userId, UUID snippetId) {
    try {
      permissionClient.createOwnerPermission(jwt.getTokenValue(), userId, snippetId);
    } catch (RestClientException | IllegalStateException ex) {
      log.warn("No se pudo registrar permiso OWNER en permission-service: {}", ex.getMessage());
    }
  }

  private void createSharedPermission(Jwt jwt, UUID snippetId, UUID targetUserId) {
    try {
      permissionClient.createSharedPermission(jwt.getTokenValue(), snippetId, targetUserId);
    } catch (RestClientException | IllegalStateException ex) {
      throw new IllegalStateException(
          "No se pudo compartir el snippet: servicio de permisos no disponible.", ex);
    }
  }

  private Snippet loadSnippetOwnedBy(UUID snippetId, UUID ownerId) {
    return snippetRepository
        .findByIdAndOwnerUserId(snippetId, ownerId)
        .orElseThrow(() -> new SnippetNotFoundException(snippetId));
  }

  private String sanitizeFilename(String original) {
    return original.replaceAll("[\\\\/:*?\"<>|]+", "_").replace(' ', '_');
  }

  private String extractExtension(String assetKey) {
    if (!StringUtils.hasText(assetKey)) {
      return "";
    }
    int lastDot = assetKey.lastIndexOf('.');
    if (lastDot < 0 || lastDot == assetKey.length() - 1) {
      return "";
    }
    return assetKey.substring(lastDot + 1);
  }
}
