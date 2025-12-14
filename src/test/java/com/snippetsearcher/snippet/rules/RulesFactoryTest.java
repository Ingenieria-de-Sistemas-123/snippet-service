package com.snippetsearcher.snippet.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class RulesFactoryTest {

  private final RulesFactory factory = new RulesFactory();

  @Test
  void providesDefaultFormattingRules() {
    var rules = factory.getDefaultFormattingRules();

    assertEquals(6, rules.size());
    assertTrue(
        rules.stream().anyMatch(r -> r.getId().equals("indentSize") && r.getValue() == 2),
        "indent size default should be 2");
  }

  @Test
  void providesDefaultLintingRules() {
    var rules = factory.getDefaultLintingRules();

    assertEquals(5, rules.size());
    Set<String> ids = rules.stream().map(Rule::getId).collect(java.util.stream.Collectors.toSet());
    assertTrue(ids.contains("no-duplicate-var"));
    assertTrue(ids.contains("println-restriction"));
  }
}
