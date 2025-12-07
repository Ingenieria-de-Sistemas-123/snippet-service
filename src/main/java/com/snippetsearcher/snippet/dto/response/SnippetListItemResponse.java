package com.snippetsearcher.snippet.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.snippetsearcher.snippet.model.Snippet;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SnippetListItemResponse(
    UUID id,
    String name,
    String language,
    String content,
    String extension,
    String author,
    String compliance) {

  public static SnippetListItemResponse fromEntity(Snippet snippet, String assetExtension) {
    return new SnippetListItemResponse(
        snippet.getId(),
        snippet.getName(),
        snippet.getLanguage(),
        "",
        assetExtension,
        snippet.getOwnerUserId() != null ? snippet.getOwnerUserId().toString() : null,
        "pending");
  }
}
