package com.snippetsearcher.snippet.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.snippetsearcher.snippet.dto.PermissionTypeDto;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.model.SnippetComplianceStatus;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SnippetListItemResponse(
    UUID id,
    String name,
    String language,
    String extension,
    String author,
    SnippetComplianceStatus complianceStatus,
    boolean valid,
    PermissionTypeDto relation) {

  public static SnippetListItemResponse fromEntity(
      Snippet snippet, String assetExtension, PermissionTypeDto relation) {
    return new SnippetListItemResponse(
        snippet.getId(),
        snippet.getName(),
        snippet.getLanguage(),
        assetExtension,
        snippet.getOwnerUserId() != null ? snippet.getOwnerUserId().toString() : null,
        snippet.getComplianceStatus(),
        snippet.getComplianceStatus() == SnippetComplianceStatus.VALID,
        relation);
  }
}
