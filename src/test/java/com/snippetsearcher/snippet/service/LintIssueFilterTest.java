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
}
