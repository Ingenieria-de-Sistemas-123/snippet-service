package com.snippetsearcher.snippet.service;

import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class PermissionService {
  private final RestClient permissionClient;

  public PermissionService(@Qualifier("permissionRestClient") RestClient permissionClient) {
    this.permissionClient = permissionClient;
  }

  public boolean canView(String userId, String snippetId) {
    // Placeholder: endpoint real puede ser /permissions/view?userId=&snippetId=
    try {
      var resp = permissionClient.get()
          .uri(uriBuilder -> uriBuilder.path("/permissions/view")
              .queryParam("userId", userId)
              .queryParam("snippetId", snippetId)
              .build())
          .retrieve()
          .body(Map.class);
      return resp != null && Boolean.TRUE.equals(resp.get("allowed"));
    } catch (Exception e) {
      // Política: si falla el permission-service devolver false (fail closed) o true (fail open). Elegimos fail closed.
      return false;
    }
  }

  public boolean canEdit(String userId, String snippetId) {
    try {
      var resp = permissionClient.get()
          .uri(uriBuilder -> uriBuilder.path("/permissions/edit")
              .queryParam("userId", userId)
              .queryParam("snippetId", snippetId)
              .build())
          .retrieve()
          .body(Map.class);
      return resp != null && Boolean.TRUE.equals(resp.get("allowed"));
    } catch (Exception e) {
      return false;
    }
  }
}

