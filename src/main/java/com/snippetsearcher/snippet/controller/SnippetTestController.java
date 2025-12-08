package com.snippetsearcher.snippet.controller;

import com.snippetsearcher.snippet.dto.request.CreateSnippetTestRequest;
import com.snippetsearcher.snippet.dto.request.UpdateSnippetTestRequest;
import com.snippetsearcher.snippet.dto.response.SnippetTestExecutionResponse;
import com.snippetsearcher.snippet.dto.response.SnippetTestResponse;
import com.snippetsearcher.snippet.service.SnippetTestService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/snippets/{snippetId}/tests")
public class SnippetTestController {

  private final SnippetTestService snippetTestService;

  public SnippetTestController(SnippetTestService snippetTestService) {
    this.snippetTestService = snippetTestService;
  }

  // UC8 - Listar tests de un snippet
  @GetMapping
  public List<SnippetTestResponse> listSnippetTests(
      @AuthenticationPrincipal Jwt jwt, @PathVariable("snippetId") UUID snippetId) {
    return snippetTestService.listSnippetTests(jwt, snippetId);
  }

  // UC8 - Crear test
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public SnippetTestResponse createSnippetTest(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable("snippetId") UUID snippetId,
      @Valid @RequestBody CreateSnippetTestRequest request) {
    return snippetTestService.createSnippetTest(jwt, snippetId, request);
  }

  // UC8 - Actualizar test
  @PutMapping("/{testId}")
  public SnippetTestResponse updateSnippetTest(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable("snippetId") UUID snippetId,
      @PathVariable("testId") UUID testId,
      @Valid @RequestBody UpdateSnippetTestRequest request) {
    return snippetTestService.updateSnippetTest(jwt, snippetId, testId, request);
  }

  // UC8 - Eliminar test
  @DeleteMapping("/{testId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteSnippetTest(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable("snippetId") UUID snippetId,
      @PathVariable("testId") UUID testId) {
    snippetTestService.deleteSnippetTest(jwt, snippetId, testId);
  }

  @PostMapping("/{testId}/execute")
  public SnippetTestExecutionResponse executeSnippetTest(
      @AuthenticationPrincipal Jwt jwt,
      @PathVariable("snippetId") UUID snippetId,
      @PathVariable("testId") UUID testId) {
    return snippetTestService.executeSnippetTest(jwt, snippetId, testId);
  }
}
