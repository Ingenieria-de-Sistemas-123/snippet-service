package com.snippetsearcher.snippet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.snippetsearcher.snippet.dto.LanguageDtos;
import com.snippetsearcher.snippet.rules.Rule;
import java.util.List;
import org.junit.jupiter.api.Test;

class LintIssueFilterTest {

  private final LintIssueFilter filter = new LintIssueFilter();

  @Test
  void filtersByActiveRulesUsingRuleId() {
    var issues =
        List.of(
            new LanguageDtos.AnalyzeIssue("no-duplicate-var", "dup", null, 1, 1, 1, 5),
            new LanguageDtos.AnalyzeIssue("println-restriction", "println", null, 2, 1, 2, 10),
            new LanguageDtos.AnalyzeIssue("string-number-concat", "concat", null, 3, 1, 3, 10));

    var rules =
        List.of(
            new Rule("no-duplicate-var", "dup", true, null),
            new Rule("println-restriction", "println", false, null));

    var filtered = filter.filter(issues, rules);

    assertEquals(1, filtered.size());
    assertEquals("no-duplicate-var", filtered.getFirst().rule());
  }

  @Test
  void infersRuleIdsFromMessagesWhenMissing() {
    var issues =
        List.of(
            new LanguageDtos.AnalyzeIssue(
                null, "Variable X ya declarada previamente", null, 1, 1, 1, 5),
            new LanguageDtos.AnalyzeIssue(
                null, "println solo admite literal o identificador", null, 2, 1, 2, 10));

    var rules =
        List.of(
            new Rule("no-duplicate-var", "dup", true, null),
            new Rule("println-restriction", "println", true, null));

    var filtered = filter.filter(issues, rules);

    assertEquals(2, filtered.size());
    assertTrue(filtered.stream().allMatch(i -> i.rule() != null));
  }

  @Test
  void empty_issues_returns_empty() {
    var res = filter.filter(List.of(), List.of());
    assertTrue(res.isEmpty());
  }

  @Test
  void filters_only_active_rules() {
    var issues =
        List.of(
            new LanguageDtos.AnalyzeIssue("no-duplicate-var", "msg", "ERROR", 1, 1, 1, 2),
            new LanguageDtos.AnalyzeIssue("println-restriction", "msg", "ERROR", 2, 1, 2, 2));

    var rules =
        List.of(
            new Rule("no-duplicate-var", "dup", true, null),
            new Rule("println-restriction", "print", false, null));

    var res = filter.filter(issues, rules);

    assertEquals(1, res.size());
    assertEquals("no-duplicate-var", res.get(0).rule());
  }

  @Test
  void infers_rule_from_message_when_missing() {
    var issues =
        List.of(
            new LanguageDtos.AnalyzeIssue(
                null, "Variable ya declarada previamente", "ERROR", 1, 1, 1, 2));

    var rules = List.of(new Rule("no-duplicate-var", "dup", true, null));

    var res = filter.filter(issues, rules);

    assertEquals(1, res.size());
    assertEquals("no-duplicate-var", res.get(0).rule());
  }

  @Test
  void unknown_message_is_dropped() {
    var issues = List.of(new LanguageDtos.AnalyzeIssue(null, "mensaje raro", "ERROR", 1, 1, 1, 2));

    var rules = List.of(new Rule("no-duplicate-var", "dup", true, null));

    var res = filter.filter(issues, rules);

    assertTrue(res.isEmpty());
  }

  @Test
  void filters_only_active_rules2() {
    Rule active = new Rule("no-duplicate-var", "", true, null);
    Rule inactive = new Rule("identifier-style", "", false, null);

    LanguageDtos.AnalyzeIssue i1 =
        new LanguageDtos.AnalyzeIssue(null, "ya declarada previamente", "ERROR", 1, 1, 1, 5);
    LanguageDtos.AnalyzeIssue i2 =
        new LanguageDtos.AnalyzeIssue("identifier-style", "msg", "WARN", 1, 1, 1, 5);

    var result = filter.filter(List.of(i1, i2), List.of(active, inactive));

    assertEquals(1, result.size());
    assertEquals("no-duplicate-var", result.get(0).rule());
  }

  @Test
  void returns_empty_when_no_issues() {
    var result = filter.filter(List.of(), List.of());

    assertTrue(result.isEmpty());
  }
}
