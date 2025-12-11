package com.snippetsearcher.snippet.dto.response;

import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.model.SnippetComplianceStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SnippetResponse(
    UUID id,
    String name,
    String language,
    String version,
    String description,
    String assetKey,
    UUID ownerUserId,
    SnippetComplianceStatus complianceStatus,
    String complianceMessage,
    String content,
    List<SnippetLintErrorResponse> lintErrors,
    List<SnippetTestResponse> tests,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt) {

  public static SnippetResponse fromEntity(
      Snippet s,
      String content,
      List<SnippetLintErrorResponse> lintErrors,
      List<SnippetTestResponse> tests,
      SnippetComplianceStatus complianceStatus,
      String complianceMessage) {
    return new SnippetResponse(
        s.getId(),
        s.getName(),
        s.getLanguage(),
        s.getVersion(),
        s.getDescription(),
        s.getAssetKey(),
        s.getOwnerUserId(),
        complianceStatus,
        complianceMessage,
        content,
        lintErrors,
        tests,
        s.getCreatedAt(),
        s.getUpdatedAt());
  }

  public static SnippetResponse fromEntity(Snippet s) {
    return fromEntity(
        s, null, List.of(), List.of(), s.getComplianceStatus(), s.getComplianceMessage());
  }
}
