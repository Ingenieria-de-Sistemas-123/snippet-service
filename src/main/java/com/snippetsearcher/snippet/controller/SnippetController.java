package com.snippetsearcher.snippet.controller;

import com.snippetsearcher.snippet.dto.request.CreateSnippetRequest;
import com.snippetsearcher.snippet.dto.response.SnippetResponse;
import com.snippetsearcher.snippet.service.SnippetService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/snippets")
public class SnippetController {

  private final SnippetService snippetService;

  public SnippetController(SnippetService snippetService) {
    this.snippetService = snippetService;
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
    String tokenValue = jwt.getTokenValue();
    return snippetService.createSnippet(tokenValue, request, file);
  }
}
