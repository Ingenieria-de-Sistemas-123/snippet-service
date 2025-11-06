// src/main/java/com/snippetsearcher/snippet/controller/SnippetController.java
package com.snippetsearcher.snippet.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.snippetsearcher.snippet.dto.LanguageDtos.*;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.repository.SnippetRepository;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/snippets")
public class SnippetController {
  private final RestClient http;
  private final SnippetRepository repo;

  public SnippetController(RestClient http, SnippetRepository repo) {
    this.http = http;
    this.repo = repo;
  }

  public record CreateSnippet(String name, String language, String version, String content) {}

  @PostMapping
  public ResponseEntity<?> create(@RequestBody CreateSnippet body) {
    String version = (body.version() == null || body.version().isBlank()) ? "1.0" : body.version();

    var validateReq = new ValidateRequest(body.language(), version, body.content());
    var validateResp =
        http.post()
            .uri("/validate")
            .contentType(APPLICATION_JSON)
            .body(validateReq)
            .retrieve()
            .body(ValidateResponse.class);

    if (validateResp == null || !validateResp.valid()) {
      return ResponseEntity.badRequest()
          .body(
              validateResp == null
                  ? Map.of(
                      "valid", false, "errors", List.of(Map.of("message", "Validation failed")))
                  : Map.of("valid", false, "errors", validateResp.errors()));
    }

    String finalContent = body.content();
    var formatReq = new FormatRequest(body.language(), version, body.content(), false);
    var formatResp =
        http.post()
            .uri("/format")
            .contentType(APPLICATION_JSON)
            .body(formatReq)
            .retrieve()
            .body(FormatResponse.class);
    if (formatResp != null && formatResp.formatted() != null && !formatResp.formatted().isBlank()) {
      finalContent = formatResp.formatted();
    }

    Snippet saved = repo.save(Snippet.of(body.name(), body.language(), finalContent));
    return ResponseEntity.ok(saved);
  }

  @GetMapping
  public List<Snippet> list() {
    return repo.findAll();
  }
}
