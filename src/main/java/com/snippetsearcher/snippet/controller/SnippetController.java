package com.snippetsearcher.snippet.controller;

import com.snippetsearcher.snippet.dto.LanguageDtos.AnalyzeResponse;
import com.snippetsearcher.snippet.dto.LanguageDtos.ExecuteResponse;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.service.SnippetService;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/snippets")
public class SnippetController {

  private final SnippetService app;

  public SnippetController(SnippetService app) {
    this.app = app;
  }

  public record CreateSnippet(String name, String description, String language, String version, String content) {
  }

  public record UpdateSnippet(
          String name,
          String description,
          String language,
          String version,
          String content
  ) {}

  public record AnalyzeBody(String language, String version, String content) {}

  public record ExecuteBody(String language, String version, String content) {}
  @PostMapping
  public ResponseEntity<?> create(@AuthenticationPrincipal Jwt jwt,
                                  @RequestBody CreateSnippet body
  ) {
    String owner = jwt.getSubject();
    try {
      Snippet saved = app.create(
              body.name(),
              body.description(),
              body.language(),
              body.version(),
              body.content()
      );
      return ResponseEntity.ok(saved);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity
              .badRequest()
              .body(Map.of("valid", false, "errors", ex.getMessage()));
    }
  }

  @PostMapping("/analyze")
  public ResponseEntity<AnalyzeResponse> analyze(@RequestBody AnalyzeBody body) {
    return ResponseEntity.ok(app.analyze(body.language(), body.version(), body.content()));
  }

  @PostMapping("/execute")
  public ResponseEntity<ExecuteResponse> execute(@RequestBody ExecuteBody body) {
    return ResponseEntity.ok(app.execute(body.language(), body.version(), body.content()));
  }

  @GetMapping
  public List<Snippet> list() {
    return app.list();
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(
          @AuthenticationPrincipal Jwt jwt,
          @PathVariable UUID id,
          @RequestBody UpdateSnippet body
  ) {
    String requesterSub = jwt.getSubject();

    try {
      Snippet updated =
              app.update(
                      id,
                      body.name(),
                      body.description(),
                      body.language(),
                      body.version(),
                      body.content(),
                      requesterSub
              );
      return ResponseEntity.ok(updated);

    } catch (IllegalArgumentException ex) {
      // puede ser "Snippet not found" o "Invalid source: ...".
      // diferenciamos rápido por el mensaje:
      if ("Snippet not found".equals(ex.getMessage())) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Snippet not found"));
      }
      // caso inválido: devolvemos el mismo formato que en create
      return ResponseEntity.badRequest()
              .body(Map.of("valid", false, "errors", ex.getMessage()));

    } catch (SecurityException ex) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
              .body(Map.of("error", ex.getMessage()));
    }
  }

  @GetMapping("/test-update")
  public String testUpdate() {
    return "Update endpoint loaded";
  }
}
