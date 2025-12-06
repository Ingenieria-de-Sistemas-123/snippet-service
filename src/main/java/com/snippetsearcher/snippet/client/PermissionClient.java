package com.snippetsearcher.snippet.client;

import com.snippetsearcher.snippet.dto.PermissionTypeDto;
import com.snippetsearcher.snippet.dto.UserAccountDto;
import com.snippetsearcher.snippet.dto.request.CreatePermissionRequestDto;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class PermissionClient {

  private final RestTemplate restTemplate;
  private final String baseUrl;

  public PermissionClient(
      RestTemplate restTemplate, @Value("${permission-service.base-url}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl; // http://permission-service:8080
  }

  /** Sincroniza/crea el usuario en permission-service usando el JWT que viene de Auth0. */
  public UserAccountDto ensureUser(String bearerToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(bearerToken);

    HttpEntity<Void> entity = new HttpEntity<>(headers);

    ResponseEntity<UserAccountDto> response =
        restTemplate.exchange(
            baseUrl + "/api/me/sync", HttpMethod.POST, entity, UserAccountDto.class);

    if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
      throw new IllegalStateException(
          "Error llamando a /api/me/sync en permission-service. Status="
              + response.getStatusCode());
    }

    return response.getBody();
  }

  /** Crea un permiso OWNER para (snippetId, userId) en permission-service. */
  public void createOwnerPermission(String bearerToken, UUID userId, UUID snippetId) {
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(bearerToken);
    headers.setContentType(MediaType.APPLICATION_JSON);

    CreatePermissionRequestDto body =
        new CreatePermissionRequestDto(snippetId, userId, PermissionTypeDto.OWNER);

    HttpEntity<CreatePermissionRequestDto> entity = new HttpEntity<>(body, headers);

    ResponseEntity<Void> response =
        restTemplate.exchange(baseUrl + "/api/permissions", HttpMethod.POST, entity, Void.class);

    if (!response.getStatusCode().is2xxSuccessful()) {
      throw new IllegalStateException(
          "Error creando permiso OWNER. Status=" + response.getStatusCode());
    }
  }
}
