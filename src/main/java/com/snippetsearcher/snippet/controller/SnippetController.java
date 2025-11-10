package com.snippetsearcher.snippet.controller;

import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.service.PermissionService;
import com.snippetsearcher.snippet.service.SnippetService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/snippets")
public class SnippetController {
  private final SnippetService service;
  private final PermissionService permissionService;

  public SnippetController(SnippetService service, PermissionService permissionService) {
    this.service = service;
      this.permissionService = permissionService;
  }

  public record CreateSnippet(String name, String description, String language, String version, String content) {}
  public record UpdateSnippet(String name, String description, String language, String version, String content) {}

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Snippet> create(@RequestBody CreateSnippet body) {
    Snippet saved = service.create(body.name(), body.description(), body.language(), body.version(), body.content());
    return ResponseEntity.ok(saved);
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Snippet> createMultipart(
      @RequestParam("name") String name,
      @RequestParam(value = "description", required = false) String description,
      @RequestParam("language") String language,
      @RequestParam("version") String version,
      @RequestPart("file") MultipartFile file) throws IOException {
    String content = new String(file.getBytes(), StandardCharsets.UTF_8);
    Snippet saved = service.create(name, description, language, version, content);
    return ResponseEntity.ok(saved);
  }

  @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Snippet> update(@PathVariable UUID id, @RequestBody UpdateSnippet body) {
    Snippet updated = service.update(id, body.name(), body.description(), body.language(), body.version(), body.content());
    return ResponseEntity.ok(updated);
  }

  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Snippet> updateMultipart(
      @PathVariable UUID id,
      @RequestParam("name") String name,
      @RequestParam(value = "description", required = false) String description,
      @RequestParam("language") String language,
      @RequestParam("version") String version,
      @RequestPart("file") MultipartFile file) throws IOException {
    String content = new String(file.getBytes(), StandardCharsets.UTF_8);
    Snippet updated = service.update(id, name, description, language, version, content);
    return ResponseEntity.ok(updated);
  }

  @GetMapping
  public List<Snippet> list() {
    return service.list();
  }

  @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Snippet> update(@PathVariable UUID id, @RequestBody UpdateSnippet body, HttpServletRequest req) {
      String auth = req.getHeader("Authorization");
      if (auth == null || !auth.startsWith("Bearer ")) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      String jwt = auth.substring("Bearer ".length());
      boolean allowed = permissionService.canEdit(jwt, id.toString(), "");
      if (!allowed) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      Snippet updated = service.update(id, body.name(), body.description(), body.language(), body.version(), body.content());
      return ResponseEntity.ok(updated);
  }

}
