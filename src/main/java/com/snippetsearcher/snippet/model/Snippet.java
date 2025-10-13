package com.snippetsearcher.snippet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
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
}