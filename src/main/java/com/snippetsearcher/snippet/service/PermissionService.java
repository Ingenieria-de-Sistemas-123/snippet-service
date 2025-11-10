package com.snippetsearcher.snippet.service;

import com.snippetsearcher.snippet.dto.PermissionDtos;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class PermissionService {
    private final RestClient permissionClient;

    public PermissionService(@Qualifier("permissionRestClient") RestClient permissionClient) {
        this.permissionClient = permissionClient;
    }

    // bearerToken: el JWT sin el prefijo "Bearer "
    public boolean authorize(String bearerToken, String action, String snippetId, String resourceOwnerSub) {
        try {
            var req = new PermissionDtos.AuthorizeRequest(action, snippetId, resourceOwnerSub == null ? "" : resourceOwnerSub);
            var resp = permissionClient.post()
                    .uri("/api/permissions/authorize")
                    .header("Authorization", "Bearer " + bearerToken)
                    .body(req)
                    .retrieve()
                    .body(PermissionDtos.AuthorizeResponse.class);
            return resp != null && resp.allowed();
        } catch (Exception e) {
            return false; // fail-closed
        }
    }

    public boolean canView(String bearerToken, String snippetId, String resourceOwnerSub) {
        return authorize(bearerToken, "READ", snippetId, resourceOwnerSub);
    }

    public boolean canEdit(String bearerToken, String snippetId, String resourceOwnerSub) {
        return authorize(bearerToken, "EDIT", snippetId, resourceOwnerSub);
    }
}
