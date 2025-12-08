package com.snippetsearcher.snippet.controller;

import com.snippetsearcher.snippet.dto.UserAccountDto;
import com.snippetsearcher.snippet.rules.Rule;
import com.snippetsearcher.snippet.service.FormattingRulesService;
import com.snippetsearcher.snippet.service.LintingRulesService;
import com.snippetsearcher.snippet.service.SnippetService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rules")
public class RulesController {

  private final FormattingRulesService formattingRulesService;
  private final LintingRulesService lintingRulesService;
  private final SnippetService snippetService;

  public RulesController(
      FormattingRulesService formattingRulesService,
      LintingRulesService lintingRulesService,
      SnippetService snippetService) {
    this.formattingRulesService = formattingRulesService;
    this.lintingRulesService = lintingRulesService;
    this.snippetService = snippetService;
  }

  // ---------- FORMATTING ----------

  @GetMapping("/formatting")
  public ResponseEntity<List<Rule>> getFormattingRules() {
    return ResponseEntity.ok(formattingRulesService.getFormattingRules());
  }

  @PostMapping("/formatting")
  public ResponseEntity<List<Rule>> updateFormattingRules(
      @AuthenticationPrincipal Jwt jwt, @RequestBody List<Rule> rules) {

    UserAccountDto user = snippetService.ensureUserForController(jwt); // ver nota abajo
    UUID adminId = user.id();
    return ResponseEntity.ok(formattingRulesService.updateFormattingRules(rules, adminId));
  }

  // ---------- LINTING ----------

  @GetMapping("/linting")
  public ResponseEntity<List<Rule>> getLintingRules() {
    return ResponseEntity.ok(lintingRulesService.getLintingRules());
  }

  @PostMapping("/linting")
  public ResponseEntity<List<Rule>> updateLintingRules(
      @AuthenticationPrincipal Jwt jwt, @RequestBody List<Rule> rules) {

    UserAccountDto user = snippetService.ensureUserForController(jwt);
    UUID adminId = user.id();
    return ResponseEntity.ok(lintingRulesService.updateLintingRules(rules, adminId));
  }
}
