package com.snippetsearcher.snippet.controller;

import com.snippetsearcher.snippet.dto.request.CreateSnippetRequest;
import com.snippetsearcher.snippet.dto.request.FormatSnippetRequest;
import com.snippetsearcher.snippet.dto.request.ShareSnippetRequest;
import com.snippetsearcher.snippet.dto.request.UpdateSnippetRequest;
import com.snippetsearcher.snippet.dto.response.FormatSnippetResponse;
import com.snippetsearcher.snippet.dto.response.ListSnippetsResponse;
import com.snippetsearcher.snippet.dto.response.SnippetResponse;
import com.snippetsearcher.snippet.service.SnippetLanguageService;
import com.snippetsearcher.snippet.service.SnippetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Validated
@RequestMapping("/api/snippets")
public class SnippetController {

  private final SnippetService snippetService;
  private final SnippetLanguageService snippetLanguageService;

  public SnippetController(
      SnippetService snippetService, SnippetLanguageService snippetLanguageService) {
    this.snippetService = snippetService;
    this.snippetLanguageService = snippetLanguageService;
  }

  /**
   * Use Case 1: crear snippet a partir de un archivo + metadatos.
   *
   * <p>Content-Type: multipart/form-data - file: archivo del snippet - request: JSON con
   * CreateSnippetRequest
   */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public SnippetResponse createSnippet(
      @AuthenticationPrincipal Jwt jwt,
      @RequestPart("file") MultipartFile file,
      @Valid @RequestPart("request") CreateSnippetRequest request) {
    return snippetService.createSnippet(jwt, request, file);
  }

  @GetMapping
  public ListSnippetsResponse listSnippets(
      @AuthenticationPrincipal Jwt jwt,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(name = "page_size", defaultValue = "10") @Min(1) @Max(100) int pageSize,
      @RequestParam(required = false) String name) {
    return snippetService.listSnippets(jwt, page, pageSize, name);
  }

  @GetMapping("/{id}")
  public SnippetResponse getSnippet(
      @AuthenticationPrincipal Jwt jwt, @PathVariable("id") UUID snippetId) {
    return snippetService.getSnippet(jwt, snippetId);
  }

  @PutMapping("/{id}")
  public SnippetResponse updateSnippet(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable("id") UUID snippetId,
      @Valid @RequestBody UpdateSnippetRequest request) {
    return snippetService.updateSnippet(jwt, snippetId, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteSnippet(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") UUID snippetId) {
    snippetService.deleteSnippet(jwt, snippetId);
  }

  @PostMapping("/{id}/share")
  public SnippetResponse shareSnippet(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable("id") UUID snippetId,
      @Valid @RequestBody ShareSnippetRequest request) {
    return snippetService.shareSnippet(jwt, snippetId, request);
  }

  @PostMapping("/format")
  public FormatSnippetResponse formatSnippet(@Valid @RequestBody FormatSnippetRequest request) {
    return snippetLanguageService.formatSnippet(request);
  }
}
