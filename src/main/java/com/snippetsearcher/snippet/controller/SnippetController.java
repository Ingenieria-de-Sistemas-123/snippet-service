package com.snippetsearcher.snippet.controller;

import com.snippetsearcher.snippet.dto.LanguageDtos;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.repository.SnippetRepository;
import com.snippetsearcher.snippet.service.LanguageValidationService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/snippets")
public class SnippetController {
  private final LanguageValidationService validator;
  private final SnippetRepository repo;

  public SnippetController(LanguageValidationService validator, SnippetRepository repo) {
    this.validator = validator;
    this.repo = repo;
  }

  public record CreateSnippet(String name, String description, String language, String version, String content) {}
  public record UpdateSnippet(String name, String description, String language, String version, String content) {}

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> create(@RequestBody CreateSnippet body) {
    LanguageDtos.ValidateResponse resp = validator.validate(body.language(), body.version(), body.content());
    if (resp == null || !resp.valid()) {
      return ResponseEntity.badRequest().body(resp == null ? Map.of("error", "Validation failed") : Map.of("valid", false, "errors", resp.errors()));
    }
    Snippet saved = repo.save(Snippet.of(body.name(), body.description(), body.language(), body.version(), body.content()));
    return ResponseEntity.ok(saved);
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> createMultipart(
      @RequestPart("name") String name,
      @RequestPart(value = "description", required = false) String description,
      @RequestPart("language") String language,
      @RequestPart("version") String version,
      @RequestPart("file") MultipartFile file) throws IOException {
    String content = new String(file.getBytes(), StandardCharsets.UTF_8);
    LanguageDtos.ValidateResponse resp = validator.validate(language, version, content);
    if (resp == null || !resp.valid()) {
      return ResponseEntity.badRequest().body(resp == null ? Map.of("error", "Validation failed") : Map.of("valid", false, "errors", resp.errors()));
    }
    Snippet saved = repo.save(Snippet.of(name, description, language, version, content));
    return ResponseEntity.ok(saved);
  }

  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<?> updateMultipart(
      @PathVariable UUID id,
      @RequestPart("name") String name,
      @RequestPart(value = "description", required = false) String description,
      @RequestPart("language") String language,
      @RequestPart("version") String version,
      @RequestPart("file") MultipartFile file) throws IOException {
    var snippet = repo.findById(id).orElse(null);
    if (snippet == null) {
      return ResponseEntity.notFound().build();
    }
    String content = new String(file.getBytes(), StandardCharsets.UTF_8);
    LanguageDtos.ValidateResponse resp = validator.validate(language, version, content);
    if (resp == null || !resp.valid()) {
      return ResponseEntity.badRequest().body(resp == null ? Map.of("error", "Validation failed") : Map.of("valid", false, "errors", resp.errors()));
    }
    snippet.update(name, description, language, version, content);
    repo.save(snippet);
    return ResponseEntity.ok(snippet);
  }

  @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody UpdateSnippet body) {
    var snippet = repo.findById(id).orElse(null);
    if (snippet == null) {
      return ResponseEntity.notFound().build();
    }
    LanguageDtos.ValidateResponse resp = validator.validate(body.language(), body.version(), body.content());
    if (resp == null || !resp.valid()) {
      return ResponseEntity.badRequest().body(resp == null ? Map.of("error", "Validation failed") : Map.of("valid", false, "errors", resp.errors()));
    }
    snippet.update(body.name(), body.description(), body.language(), body.version(), body.content());
    repo.save(snippet);
    return ResponseEntity.ok(snippet);
  }

  @GetMapping
  public List<Snippet> list() {
    return repo.findAll();
  }
}
