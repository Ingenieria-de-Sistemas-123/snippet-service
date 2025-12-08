// src/main/java/com/snippetsearcher/snippet/rules/RulesFactory.java
package com.snippetsearcher.snippet.rules;

import java.util.List;

public class RulesFactory {

  public List<Rule> getDefaultFormattingRules() {
    return List.of(
        new Rule("1", "space-before-colon", true, null),
        new Rule("2", "space-after-colon", false, null),
        new Rule("3", "space-around-equals", true, null),
        new Rule("4", "newline-before-println", false, 0),
        new Rule("5", "indentation", false, 4));
  }

  public List<Rule> getDefaultLintingRules() {
    return List.of(
        new Rule("1", "snake-case-variables", true, null),
        new Rule("2", "camel-case-variables", true, null),
        new Rule("3", "mandatory-variable-or-literal-in-println", true, null),
        new Rule("4", "read-input-with-simple-argument", true, null));
  }
}
