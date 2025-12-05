package com.snippetsearcher.snippet.service;

import com.snippetsearcher.snippet.client.AssetClient;
import com.snippetsearcher.snippet.client.PermissionClient;
import com.snippetsearcher.snippet.dto.UserAccountDto;
import com.snippetsearcher.snippet.dto.request.CreateSnippetRequest;
import com.snippetsearcher.snippet.dto.response.SnippetResponse;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.repository.SnippetRepository;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class SnippetService {

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
    public SnippetResponse createSnippet(
            String tokenValue, CreateSnippetRequest request, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo del snippet es obligatorio.");
        }

        // 1) Asegurar/obtener usuario en permission-service
        UserAccountDto user = permissionClient.ensureUser(tokenValue);
        UUID userId = user.id();

        // 2) Subir archivo a asset-service
        String originalFilename =
                (file.getOriginalFilename() != null && !file.getOriginalFilename().isBlank())
                        ? file.getOriginalFilename()
                        : "snippet.ps";

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
        permissionClient.createOwnerPermission(tokenValue, userId, snippet.getId());

        // 5) Devolver respuesta
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
                                        request.language(),
                                        firstError.message(),
                                        firstError.line(),
                                        firstError.col()));
            }

            throw new IllegalArgumentException(
                    "El snippet no es válido para el lenguaje %s.".formatted(request.language()));
        }
    }
}