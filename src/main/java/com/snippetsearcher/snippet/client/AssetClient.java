package com.snippetsearcher.snippet.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

@Component
public class AssetClient {

  private final RestTemplate restTemplate;
  private final String baseUrl;

  public AssetClient(@Value("${asset-service.base-url}") String baseUrl) {
    this.baseUrl = baseUrl;
    this.restTemplate = new RestTemplate();
  }

  /**
   * Sube el contenido del snippet a asset-service.
   *
   * @param container por ejemplo "snippets"
   * @param key nombre único dentro del container (ej: uuid-nombre.ps)
   * @param content bytes del archivo
   * @param contentType MIME type (o null si no se conoce)
   * @return assetKey que usaremos para referenciar este asset (container/key)
   */
  public String uploadSnippet(String container, String key, byte[] content, String contentType) {
    HttpHeaders headers = new HttpHeaders();
    if (contentType != null && !contentType.isBlank()) {
      headers.setContentType(MediaType.parseMediaType(contentType));
    } else {
      headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    }

    HttpEntity<byte[]> entity = new HttpEntity<>(content, headers);

    ResponseEntity<Void> response =
        restTemplate.exchange(
            baseUrl + "/v1/asset/" + container + "/" + key, HttpMethod.PUT, entity, Void.class);

    if (!response.getStatusCode().is2xxSuccessful()) {
      throw new IllegalStateException(
          "Error subiendo asset a asset-service. Status=" + response.getStatusCode());
    }

    // Este "assetKey" es lo que vamos a guardar en la tabla de snippets.
    return container + "/" + key;
  }

  public byte[] downloadSnippet(String assetKey) {
    if (!StringUtils.hasText(assetKey)) {
      throw new IllegalArgumentException("El assetKey del snippet es obligatorio.");
    }

    HttpHeaders headers = new HttpHeaders();
    HttpEntity<Void> entity = new HttpEntity<>(headers);

    ResponseEntity<byte[]> response =
        restTemplate.exchange(
            baseUrl + "/v1/asset/" + assetKey, HttpMethod.GET, entity, byte[].class);

    if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
      throw new IllegalStateException(
          "No se pudo descargar el snippet desde asset-service. Status="
              + response.getStatusCode());
    }

    return response.getBody();
  }
}
