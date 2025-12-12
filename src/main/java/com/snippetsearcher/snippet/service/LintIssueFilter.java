package com.snippetsearcher.snippet.service;

import com.snippetsearcher.snippet.dto.LanguageDtos;
import com.snippetsearcher.snippet.rules.Rule;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Filters issues returned by the PrintScript linter so that only the user-enabled rules are
 * surfaced. If the language-service does not include rule ids, we infer them from the message
 * string using the known messages of the CLI.
 */
@Component
public class LintIssueFilter {

  /**
   * @param issues raw issues from language-service
   * @param configuredRules rules persisted by the user (asset-service)
   * @return issues restricted to active rules
   */
  public List<LanguageDtos.AnalyzeIssue> filter(
      List<LanguageDtos.AnalyzeIssue> issues, List<Rule> configuredRules) {

    if (issues == null || issues.isEmpty()) return List.of();
    Set<String> activeRuleIds =
        configuredRules.stream()
            .filter(Rule::isActive)
            .map(Rule::getId)
            .collect(Collectors.toSet());

    return issues.stream()
        .map(this::withResolvedRule)
        .filter(i -> StringUtils.hasText(i.rule()) && activeRuleIds.contains(i.rule()))
        .toList();
  }

  private LanguageDtos.AnalyzeIssue withResolvedRule(LanguageDtos.AnalyzeIssue issue) {
    if (StringUtils.hasText(issue.rule())) {
      return issue;
    }
    String inferred = inferRuleIdFromMessage(issue.message());
    return new LanguageDtos.AnalyzeIssue(
        inferred,
        issue.message(),
        issue.severity(),
        issue.startLine(),
        issue.startCol(),
        issue.endLine(),
        issue.endCol());
  }

  /**
   * The CLI does not print ruleId, so we match against the current messages emitted by each rule.
   * This is brittle but keeps behavior configurable without changing the CLI output.
   */
  private String inferRuleIdFromMessage(String message) {
    if (!StringUtils.hasText(message)) return null;
    String m = message.toLowerCase(Locale.ROOT);
    if (m.contains("ya declarada previamente")) return "no-duplicate-var";
    if (m.contains("identificador") && m.contains("estilo")) return "identifier-style";
    if (m.contains("println") && m.contains("literal")) return "println-restriction";
    if (m.contains("concatenación mixta") || m.contains("concat")) return "string-number-concat";
    if (m.contains("readinput") && m.contains("prompt")) return "read-input-prompt";
    return null;
  }
}
