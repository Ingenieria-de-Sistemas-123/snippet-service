package com.snippetsearcher.snippet.controller;

import com.snippetsearcher.snippet.service.SnippetService;
import com.snippetsearcher.snippet.dto.LanguageDtos.AnalyzeResponse;
import com.snippetsearcher.snippet.dto.LanguageDtos.ExecuteResponse;
import com.snippetsearcher.snippet.model.Snippet;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/snippets")
public class SnippetController {

  private final SnippetService app;

  public SnippetController(SnippetService app) { this.app = app; }

  public record CreateSnippet(String name, String language, String version, String content) {}
  public record AnalyzeBody(String language, String version, String content) {}
  public record ExecuteBody(String language, String version, String content) {}

  @PostMapping
  public ResponseEntity<?> create(@RequestBody CreateSnippet body) {
    try {
      Snippet saved = app.create(body.name(), body.language(), body.version(), body.content());
      return ResponseEntity.ok(saved);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(Map.of("valid", false, "errors", ex.getMessage()));
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
}
