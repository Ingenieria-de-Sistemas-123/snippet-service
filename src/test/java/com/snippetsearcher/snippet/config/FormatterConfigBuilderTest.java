package com.snippetsearcher.snippet.config;

import static org.junit.jupiter.api.Assertions.*;

import com.snippetsearcher.snippet.rules.Rule;
import com.snippetsearcher.snippet.service.FormatterConfigBuilder;
import java.util.List;
import org.junit.jupiter.api.Test;

class FormatterConfigBuilderTest {

  private final FormatterConfigBuilder builder = new FormatterConfigBuilder();

  @Test
  void builds_json_with_active_rules() {
    Rule r1 = new Rule("spaceBeforeColon", "", true, null);
    Rule r2 = new Rule("indentSize", "", true, 4);
    Rule r3 = new Rule("spaceAfterColon", "", false, null);

    String json = builder.buildConfigJson(List.of(r1, r2, r3));

    assertTrue(json.contains("spaceBeforeColon"));
    assertTrue(json.contains("indentSize"));
    assertFalse(json.contains("spaceAfterColon"));
  }

  @Test
  void ignores_unknown_rules() {
    Rule r = new Rule("unknown-rule", "", true, null);

    String json = builder.buildConfigJson(List.of(r));

    assertEquals("{}", json);
  }
}
