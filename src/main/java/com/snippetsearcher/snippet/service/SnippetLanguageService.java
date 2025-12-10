// src/main/java/com/snippetsearcher/snippet/service/SnippetLanguageService.java
package com.snippetsearcher.snippet.service;

import com.snippetsearcher.snippet.client.language.LanguageClient;
import com.snippetsearcher.snippet.dto.LanguageDtos;
import com.snippetsearcher.snippet.dto.request.FormatSnippetRequest;
import com.snippetsearcher.snippet.dto.response.FormatSnippetResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SnippetLanguageService {

  private final LanguageClient languageClient;
  private final FormattingRulesService formattingRulesService;

  public SnippetLanguageService(
          LanguageClient languageClient,
          FormattingRulesService formattingRulesService) {

    this.languageClient = languageClient;
    this.formattingRulesService = formattingRulesService;
  }

  public FormatSnippetResponse formatSnippet(FormatSnippetRequest request) {
    if (!StringUtils.hasText(request.content())) {
      throw new IllegalArgumentException("El contenido a formatear es obligatorio.");
    }

    if (!StringUtils.hasText(request.language())) {
      return new FormatSnippetResponse(
              false,
              request.content(),
              request.content(),
              "Lenguaje no provisto: se retorna igual.");
    }

    // 1) Construir el JSON de configuración de formatter según las reglas actuales
    String configJson = formattingRulesService.buildFormatterConfigJsonFromStoredRules();

    // 2) Armar el request para el language-service, incluyendo configJson
    LanguageDtos.FormatRequest formatRequest =
            new LanguageDtos.FormatRequest(
                    request.language(),
                    request.version(),
                    request.content(),
                    request.check(),
                    configJson // <<--- NUEVO: JSON de reglas de formatting
            );

    LanguageDtos.FormatResponse response;
    try {
      response = languageClient.format(formatRequest);
    } catch (Exception ex) {
      throw new IllegalStateException("Error formateando snippet en language-service.", ex);
    }

    String formatted =
            StringUtils.hasText(response.formatted()) ? response.formatted() : request.content();

    return new FormatSnippetResponse(
            response.changed(), formatted, request.content(), response.diagnostics());
  }
}
