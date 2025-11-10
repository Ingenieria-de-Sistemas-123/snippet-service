package com.snippetsearcher.snippet.service;

import com.snippetsearcher.snippet.dto.LanguageDtos;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class LanguageValidationService {
  private final RestClient http;

  public LanguageValidationService(@Qualifier("languageRestClient") RestClient http) {
    this.http = http;
  }

  public LanguageDtos.ValidateResponse validate(String language, String version, String content) {
    try {
      var req = new LanguageDtos.ValidateRequest(language, version, content);
      return http.post().uri("/validate").body(req).retrieve().body(LanguageDtos.ValidateResponse.class);
    } catch (Exception e) {
      // Placeholder: en producción, mapear errores/transient errors y circuit breaker
      return null;
    }
  }
}
