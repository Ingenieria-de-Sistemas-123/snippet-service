package com.snippetsearcher.snippet.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "snippet")
public class Snippet {
  @Id private UUID id;
  private String name;
  private String description; // nueva descripcion
  private String language;
  private String version; // version del lenguaje

  @Column(columnDefinition = "TEXT")
  private String content;

  private Instant createdAt;
  private Instant updatedAt;

  public static Snippet of(String name, String description, String language, String version, String content) {
    Snippet s = new Snippet();
    s.id = UUID.randomUUID();
    s.name = name;
    s.description = description;
    s.language = language;
    s.version = version;
    s.content = content;
    s.createdAt = Instant.now();
    s.updatedAt = s.createdAt;
    return s;
  }

  public void update(String name, String description, String language, String version, String content) {
    this.name = name;
    this.description = description;
    this.language = language;
    this.version = version;
    this.content = content;
    this.updatedAt = Instant.now();
  }
}
