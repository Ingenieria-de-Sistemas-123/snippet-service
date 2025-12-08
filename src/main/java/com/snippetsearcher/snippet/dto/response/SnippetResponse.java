package com.snippetsearcher.snippet.dto.response;

import com.snippetsearcher.snippet.model.Snippet;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SnippetResponse(
    UUID id,
    String name,
    String language,
    String version,
    String description,
    String assetKey,
    UUID ownerUserId,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {

  public static SnippetResponse fromEntity(Snippet s) {
    return new SnippetResponse(
        s.getId(),
        s.getName(),
        s.getLanguage(),
        s.getVersion(),
        s.getDescription(),
        s.getAssetKey(),
        s.getOwnerUserId(),
        s.getCreatedAt(),
        s.getUpdatedAt());
  }
}
