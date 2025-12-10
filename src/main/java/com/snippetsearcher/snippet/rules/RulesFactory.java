package com.snippetsearcher.snippet.rules;

import java.util.List;

public class RulesFactory {

  public List<Rule> getDefaultFormattingRules() {
    return List.of(
            new Rule("spaceBeforeColon", "Espacio antes de ':'", true, null),
            new Rule("spaceAfterColon", "Espacio después de ':'", true, null),
            new Rule("spaceAroundEquals", "Espacio alrededor de '='", true, null),
            new Rule("spaceAroundOperators", "Espacio alrededor de operadores", false, null),
            new Rule("lineJumpAfterSemicolon", "Salto de línea tras ';'", false, null),
            new Rule("indentSize", "Tamaño de indentación", false, 2)
    );
  }

  public List<Rule> getDefaultLintingRules() {
    return List.of(
            new Rule("no-duplicate-var", "Variable ya declarada previamente", true, null),
            new Rule(
                    "identifier-style",
                    "Identificadores con estilo (camelCase o snake_case)",
                    false,
                    null),
            new Rule(
                    "println-restriction",
                    "println solo admite literal o identificador",
                    true,
                    null),
            new Rule(
                    "string-number-concat",
                    "Concat de string con number con '+'",
                    true,
                    null),
            new Rule(
                    "read-input-prompt",
                    "readInput con prompt string o identificador",
                    true,
                    null)
    );
  }
}
