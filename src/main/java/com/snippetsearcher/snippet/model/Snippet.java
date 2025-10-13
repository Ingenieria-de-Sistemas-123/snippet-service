package com.snippetsearcher.snippet.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "snippet")
public class Snippet {
    @Id
    private UUID id;
    private String name;
    private String language;
    @Column(columnDefinition = "TEXT")
    private String content;
    private Instant createdAt;

    public static Snippet of(String name, String language, String content) {
        Snippet s = new Snippet();
        s.id = UUID.randomUUID();
        s.name = name;
        s.language = language;
        s.content = content;
        s.createdAt = Instant.now();
        return s;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLanguage() {
        return language;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setName(String v) {
        this.name = v;
    }

    public void setLanguage(String v) {
        this.language = v;
    }

    public void setContent(String v) {
        this.content = v;
    }

    public void setCreatedAt(Instant v) {
        this.createdAt = v;
    }
}