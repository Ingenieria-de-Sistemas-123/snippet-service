package com.snippetsearcher.snippet.service;

import com.snippetsearcher.snippet.client.AssetClient;
import com.snippetsearcher.snippet.client.PermissionClient;
import com.snippetsearcher.snippet.client.language.LanguageClient;
import com.snippetsearcher.snippet.dto.LanguageDtos;
import com.snippetsearcher.snippet.dto.PermissionTypeDto;
import com.snippetsearcher.snippet.dto.SnippetPermissionDto;
import com.snippetsearcher.snippet.dto.UserAccountDto;
import com.snippetsearcher.snippet.dto.request.CreateSnippetRequest;
import com.snippetsearcher.snippet.dto.request.ListSnippetsQuery;
import com.snippetsearcher.snippet.dto.request.ShareSnippetRequest;
import com.snippetsearcher.snippet.dto.request.UpdateSnippetRequest;
import com.snippetsearcher.snippet.dto.response.ListSnippetsResponse;
import com.snippetsearcher.snippet.dto.response.SnippetLintErrorResponse;
import com.snippetsearcher.snippet.dto.response.SnippetListItemResponse;
import com.snippetsearcher.snippet.dto.response.SnippetResponse;
import com.snippetsearcher.snippet.dto.response.SnippetTestResponse;
import com.snippetsearcher.snippet.exception.SnippetNotFoundException;
import com.snippetsearcher.snippet.jobs.SnippetJobProducer;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.model.SnippetComplianceStatus;
import com.snippetsearcher.snippet.model.SnippetTest;
import com.snippetsearcher.snippet.repository.SnippetRepository;
import com.snippetsearcher.snippet.repository.SnippetTestRepository;
import com.snippetsearcher.snippet.repository.specification.SnippetSpecifications;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SnippetService {

  private static final Logger log = LoggerFactory.getLogger(SnippetService.class);
  private static final String UNSPECIFIED_VERSION_VALUE = "unspecified";

  private final SnippetRepository snippetRepository;
  private final AssetClient assetClient;
  private final PermissionClient permissionClient;
  private final LanguageClient languageClient;
  private final SnippetTestRepository snippetTestRepository;
  private final SnippetJobProducer jobProducer;
  private final LintingRulesService lintingRulesService;
  private final LintIssueFilter lintIssueFilter;
  private final String snippetsContainer;

  public SnippetService(
      SnippetRepository snippetRepository,
      AssetClient assetClient,
      PermissionClient permissionClient,
      LanguageClient languageClient,
      SnippetTestRepository snippetTestRepository,
      SnippetJobProducer jobProducer,
      LintingRulesService lintingRulesService,
      LintIssueFilter lintIssueFilter,
      @Value("${asset-service.snippets-container:snippets}") String snippetsContainer) {
    this.snippetRepository = snippetRepository;
    this.assetClient = assetClient;
    this.permissionClient = permissionClient;
    this.languageClient = languageClient;
    this.snippetTestRepository = snippetTestRepository;
    this.jobProducer = jobProducer;
    this.lintingRulesService = lintingRulesService;
    this.lintIssueFilter = lintIssueFilter;
    this.snippetsContainer = snippetsContainer;
  }

  /**
   * Use Case 1: - asegura usuario en permission-service - sube archivo a asset-service - crea
   * snippet en DB - crea permiso OWNER en permission-service
   */
  @Transactional
  public SnippetResponse createSnippet(Jwt jwt, CreateSnippetRequest request, MultipartFile file) {
    // 1) Asegurar/obtener usuario en permission-service
    UserAccountDto user = ensureUser(jwt);
    UUID userId = user.id();

    // 2) Validar y subir archivo a asset-service
    String normalizedVersion = normalizeVersion(request.version());
    String normalizedDescription = normalizeDescription(request.description());
    String assetKey = uploadValidatedSnippetContent(file, request.language(), normalizedVersion);

    // 3) Crear snippet en DB local
    Snippet snippet =
        new Snippet(
            request.name(),
            request.language(),
            normalizedVersion,
            normalizedDescription,
            assetKey,
            userId);
    markSnippetValid(snippet);
    snippet = snippetRepository.save(snippet);

    // 4) Crear permiso OWNER en permission-service
    registerOwnerPermission(jwt, userId, snippet.getId());

    // 5) Devolver respuesta
    return SnippetResponse.fromEntity(snippet);
  }

  @Transactional(readOnly = true)
  public ListSnippetsResponse listSnippets(Jwt jwt, ListSnippetsQuery query) {
    UserAccountDto user = ensureUser(jwt);
    List<SnippetPermissionDto> sharedPermissions =
        query.relation().includesShared() ? loadSnippetPermissions(jwt) : List.of();

    Set<UUID> sharedSnippetIds =
        sharedPermissions.stream()
            .filter(dto -> dto.type() == PermissionTypeDto.SHARED)
            .map(SnippetPermissionDto::snippetId)
            .collect(Collectors.toSet());

    Specification<Snippet> specification = buildSpecification(user.id(), query, sharedSnippetIds);

    Pageable pageable = PageRequest.of(query.page(), query.pageSize(), query.sort());
    var snippetsPage = snippetRepository.findAll(specification, pageable);

    Map<UUID, PermissionTypeDto> relationLookup =
        sharedPermissions.stream()
            .collect(
                Collectors.toMap(
                    SnippetPermissionDto::snippetId,
                    SnippetPermissionDto::type,
                    (existing, ignored) -> existing));

    List<SnippetListItemResponse> items =
        snippetsPage.getContent().stream()
            .map(
                snippet ->
                    SnippetListItemResponse.fromEntity(
                        snippet,
                        extractExtension(snippet.getAssetKey()),
                        determineRelation(snippet, user.id(), relationLookup)))
            .toList();

    return new ListSnippetsResponse(
        snippetsPage.getNumber(), snippetsPage.getSize(), snippetsPage.getTotalElements(), items);
  }

  @Transactional(readOnly = true)
  public SnippetResponse getSnippet(Jwt jwt, UUID snippetId) {
    UserAccountDto user = ensureUser(jwt);
    Snippet snippet = loadSnippetOwnedBy(snippetId, user.id());
    String content = downloadSnippetContent(snippet);
    List<SnippetLintErrorResponse> lintErrors =
        collectLintErrors(snippet.getLanguage(), snippet.getVersion(), content);
    List<SnippetTestResponse> tests =
        snippetTestRepository.findBySnippetId(snippet.getId()).stream()
            .map(SnippetTestResponse::fromEntity)
            .toList();
    SnippetComplianceStatus complianceStatus =
        lintErrors.isEmpty() ? SnippetComplianceStatus.VALID : SnippetComplianceStatus.INVALID;
    String complianceMessage = lintErrors.isEmpty() ? null : lintErrors.getFirst().message();
    return SnippetResponse.fromEntity(
        snippet, content, lintErrors, tests, complianceStatus, complianceMessage);
  }

  @Transactional
  public SnippetResponse updateSnippet(
      Jwt jwt, UUID snippetId, UpdateSnippetRequest request, MultipartFile file) {
    UserAccountDto user = ensureUser(jwt);
    Snippet snippet = loadSnippetOwnedBy(snippetId, user.id());
    String resolvedName = resolveUpdatedName(request);
    String resolvedDescription = resolveUpdatedDescription(snippet, request);
    String resolvedVersion = resolveUpdatedVersion(snippet, request);
    String assetKey = uploadValidatedSnippetContent(file, request.language(), resolvedVersion);
    snippet.setName(resolvedName);
    snippet.setLanguage(request.language());
    snippet.setDescription(resolvedDescription);
    snippet.setVersion(resolvedVersion);
    snippet.setAssetKey(assetKey);
    markSnippetValid(snippet);
    snippet = snippetRepository.save(snippet);

    jobProducer.enqueueRunAllTestsForSnippet(snippet.getId());

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

  public UserAccountDto ensureUserForController(Jwt jwt) {
    return ensureUser(jwt);
  }

  private String uploadValidatedSnippetContent(
      MultipartFile file, String language, String version) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("El archivo del snippet es obligatorio.");
    }

    String originalFilename =
        (file.getOriginalFilename() != null && !file.getOriginalFilename().isBlank())
            ? sanitizeFilename(file.getOriginalFilename())
            : "snippet.prs";

    String key = UUID.randomUUID() + "-" + originalFilename;
    byte[] content = readFileContent(file);
    validateLanguageOrThrow(language, version, content);

    return assetClient.uploadSnippet(snippetsContainer, key, content, file.getContentType());
  }

  private byte[] readFileContent(MultipartFile file) {
    try {
      return file.getBytes();
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo leer el contenido del archivo del snippet.", e);
    }
  }

  private void validateLanguageOrThrow(String language, String version, byte[] content) {
    List<SnippetLintErrorResponse> errors =
        collectLintErrors(language, version, new String(content, StandardCharsets.UTF_8));

    if (!errors.isEmpty()) {
      SnippetLintErrorResponse firstError = errors.getFirst();
      String violatedRule =
          StringUtils.hasText(firstError.rule()) ? firstError.rule() : "desconocida";
      throw new IllegalArgumentException(
          "El snippet no es válido para el lenguaje %s (regla %s): %s (línea %d, columna %d)."
              .formatted(
                  language,
                  violatedRule,
                  firstError.message(),
                  firstError.line() != null ? firstError.line() : -1,
                  firstError.column() != null ? firstError.column() : -1));
    }
  }

  private String downloadSnippetContent(Snippet snippet) {
    try {
      byte[] data = assetClient.downloadSnippet(snippet.getAssetKey());
      return new String(data, StandardCharsets.UTF_8);
    } catch (RestClientException | IllegalStateException ex) {
      throw new IllegalStateException("No se pudo obtener el contenido del snippet.", ex);
    }
  }

  private List<SnippetLintErrorResponse> collectLintErrors(
      String language, String version, String content) {
    LanguageDtos.AnalyzeResponse response =
        languageClient.analyze(
            new LanguageDtos.AnalyzeRequest(language, normalizeVersion(version), content));

    List<LanguageDtos.AnalyzeIssue> filtered =
        lintIssueFilter.filter(
            response != null ? response.issues() : List.of(), lintingRulesService.getLintingRules());

    return filtered.stream()
        .map(
            i ->
                new SnippetLintErrorResponse(
                    i.rule(), i.startLine(), i.startCol(), i.message()))
        .toList();
  }

  private String buildExecutableContent(String snippetContent, String testScript) {
    if (!StringUtils.hasText(testScript)) {
      throw new IllegalArgumentException("El script del test es obligatorio.");
    }
    return snippetContent + System.lineSeparator() + System.lineSeparator() + testScript;
  }

  private LanguageDtos.ExecuteResponse executeTest(Snippet snippet, String executableContent) {
    try {
      return languageClient.execute(
          new LanguageDtos.ExecuteRequest(
              snippet.getLanguage(), normalizeVersion(snippet.getVersion()), executableContent));
    } catch (Exception ex) {
      throw new IllegalStateException("No se pudo ejecutar el test del snippet.", ex);
    }
  }

  private void updateTestResult(SnippetTest test, LanguageDtos.ExecuteResponse response) {
    test.setLastRunAt(OffsetDateTime.now());
    test.setLastRunExitCode(response.exitCode());
    test.setLastRunOutput(response.stdout());
    test.setLastRunError(response.stderr());
  }

  private String normalizeVersion(String version) {
    if (!StringUtils.hasText(version)) {
      return null;
    }
    String trimmed = version.trim();
    return UNSPECIFIED_VERSION_VALUE.equalsIgnoreCase(trimmed) ? null : trimmed;
  }

  private String normalizeDescription(String description) {
    return StringUtils.hasText(description) ? description.trim() : null;
  }

  private String resolveUpdatedName(UpdateSnippetRequest request) {
    if (!StringUtils.hasText(request.name())) {
      throw new IllegalArgumentException("El nombre es obligatorio.");
    }
    return request.name().trim();
  }

  private String resolveUpdatedDescription(Snippet snippet, UpdateSnippetRequest request) {
    return request.description() == null
        ? snippet.getDescription()
        : normalizeDescription(request.description());
  }

  private String resolveUpdatedVersion(Snippet snippet, UpdateSnippetRequest request) {
    return request.version() == null
        ? normalizeVersion(snippet.getVersion())
        : normalizeVersion(request.version());
  }

  private Specification<Snippet> buildSpecification(
      UUID userId, ListSnippetsQuery query, Set<UUID> sharedSnippetIds) {

    Specification<Snippet> spec =
        switch (query.relation()) {
          case OWNED -> SnippetSpecifications.ownedBy(userId);
          case SHARED -> SnippetSpecifications.withIds(sharedSnippetIds);
          case ALL -> {
            Specification<Snippet> ownerSpec = SnippetSpecifications.ownedBy(userId);
            Specification<Snippet> sharedSpec = SnippetSpecifications.withIds(sharedSnippetIds);
            yield sharedSnippetIds.isEmpty() ? ownerSpec : ownerSpec.or(sharedSpec);
          }
        };
    spec = and(spec, SnippetSpecifications.nameContains(query.name()));
    spec = and(spec, SnippetSpecifications.languageEquals(query.language()));
    spec = and(spec, SnippetSpecifications.withComplianceStatus(query.complianceFilter()));
    return spec;
  }

  private Specification<Snippet> and(Specification<Snippet> base, Specification<Snippet> addition) {
    return addition == null ? base : base.and(addition);
  }

  private PermissionTypeDto determineRelation(
      Snippet snippet, UUID currentUserId, Map<UUID, PermissionTypeDto> relationLookup) {
    if (snippet.getOwnerUserId() != null && snippet.getOwnerUserId().equals(currentUserId)) {
      return PermissionTypeDto.OWNER;
    }
    return relationLookup.getOrDefault(snippet.getId(), PermissionTypeDto.SHARED);
  }

  private List<SnippetPermissionDto> loadSnippetPermissions(Jwt jwt) {
    try {
      return permissionClient.listSnippetPermissions(jwt.getTokenValue());
    } catch (RestClientException | IllegalStateException ex) {
      log.warn(
          "No se pudo obtener snippets compartidos del permission-service: {}", ex.getMessage());
      return List.of();
    }
  }

  private void markSnippetValid(Snippet snippet) {
    snippet.setComplianceStatus(SnippetComplianceStatus.VALID);
    snippet.setComplianceMessage(null);
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
