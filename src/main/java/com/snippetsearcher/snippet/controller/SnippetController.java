package com.snippetsearcher.snippet.controller;

import com.snippetsearcher.snippet.dto.LanguageDtos;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.repository.SnippetRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/snippets")
public class SnippetController {
    private final RestClient http;
    private final SnippetRepository repo;

    public SnippetController(RestClient http, SnippetRepository repo) {
        this.http = http;
        this.repo = repo;
    }

    public record CreateSnippet(String name, String language, String content) {
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateSnippet body) {
        var req = new LanguageDtos.ValidateRequest(body.language(), body.content());
        var resp = http.post().uri("/validate").body(req).retrieve().body(LanguageDtos.ValidateResponse.class);
        if (resp == null || !resp.valid()) {
            return ResponseEntity.badRequest().body(resp == null ? Map.of("error", "Validation failed")
                    : Map.of("valid", false, "errors", resp.errors()));
        }
        Snippet saved = repo.save(Snippet.of(body.name(), body.language(), body.content()));
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public List<Snippet> list() {
        return repo.findAll();
    }
}
