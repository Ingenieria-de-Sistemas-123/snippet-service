package com.snippetsearcher.snippet.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "snippets")
public class Snippet {

  @Id
  @GeneratedValue
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(nullable = false, length = 50)
  private String language;

  @Column(nullable = false, length = 20)
  private String version;

  @Column(length = 1000)
  private String description;

  @Column(name = "asset_key", nullable = false, unique = true, length = 300)
  private String assetKey;

  @Column(name = "owner_user_id", nullable = false, columnDefinition = "uuid")
  private UUID ownerUserId;

  @Enumerated(EnumType.STRING)
  @Column(name = "compliance_status", nullable = false, length = 20)
  private SnippetComplianceStatus complianceStatus = SnippetComplianceStatus.UNKNOWN;

  @Column(name = "compliance_message", length = 500)
  private String complianceMessage;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt = OffsetDateTime.now();

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt = OffsetDateTime.now();

  protected Snippet() {
    // JPA
  }

  public Snippet(
      String name,
      String language,
      String version,
      String description,
      String assetKey,
      UUID ownerUserId) {
    this.name = name;
    this.language = language;
    this.version = version;
    this.description = description;
    this.assetKey = assetKey;
    this.ownerUserId = ownerUserId;
    this.complianceStatus = SnippetComplianceStatus.VALID;
  }

  @PreUpdate
  public void touchUpdatedAt() {
    this.updatedAt = OffsetDateTime.now();
  }
}
