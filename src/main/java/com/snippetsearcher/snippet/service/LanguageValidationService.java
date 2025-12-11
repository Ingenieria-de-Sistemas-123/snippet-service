package com.snippetsearcher.snippet.service;

import com.snippetsearcher.snippet.dto.LanguageDtos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
public class LanguageValidationService {
  private static final Logger log = LoggerFactory.getLogger(LanguageValidationService.class);
  private final RestClient http;

  public LanguageValidationService(RestClient http) {
    this.http = http;
  }

  public LanguageDtos.ValidateResponse validate(String language, String version, String content) {
    var req = new LanguageDtos.ValidateRequest(language, version, content);
    try {
      return http.post()
          .uri("/validate")
          .body(req)
          .retrieve()
          .body(LanguageDtos.ValidateResponse.class);
    } catch (RestClientResponseException ex) {
      log.warn(
          "Error validando snippet en language-service. status={}, body={}",
          ex.getStatusCode(),
          ex.getResponseBodyAsString());
      throw new IllegalStateException(
          "No se pudo validar el snippet: language-service rechazó la solicitud.", ex);
    } catch (RestClientException ex) {
      log.error("Fallo comunicándose con language-service para validar snippet.", ex);
      throw new IllegalStateException(
          "No se pudo validar el snippet: language-service no disponible.", ex);
    }
  }
}
