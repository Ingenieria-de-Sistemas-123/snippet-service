package com.snippetsearcher.snippet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippetsearcher.snippet.rules.Rule;
import java.util.List;
import org.junit.jupiter.api.Test;

class FormatterConfigBuilderTest {

  private final FormatterConfigBuilder builder = new FormatterConfigBuilder();
  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void buildsBooleanFlagsForActiveRules() throws Exception {
    String json =
        builder.buildConfigJson(
            List.of(
                new Rule("spaceBeforeColon", "space before :", true, null),
                new Rule("spaceAroundEquals", "equals", false, null),
                new Rule("lineJumpAfterSemicolon", "semicolon", true, null)));

    JsonNode node = mapper.readTree(json);
    assertTrue(node.get("spaceBeforeColon").asBoolean());
    assertTrue(node.get("lineJumpAfterSemicolon").asBoolean());
    assertFalse(node.has("spaceAroundEquals"), "inactive rule should not be present");
  }

  @Test
  void writesNumericValuesForIndentSize() throws Exception {
    String json = builder.buildConfigJson(List.of(new Rule("indentSize", "indent", true, 4)));

    JsonNode node = mapper.readTree(json);
    assertEquals(4, node.get("indentSize").asInt());
  }

  @Test
  void builds_json_only_for_active_rules() {
    var rules =
        List.of(
            new Rule("spaceBeforeColon", "a", true, null),
            new Rule("spaceAfterColon", "b", false, null),
            new Rule("indentSize", "c", true, 4));

    String json = builder.buildConfigJson(rules);

    assertTrue(json.contains("spaceBeforeColon"));
    assertTrue(json.contains("indentSize"));
    assertFalse(json.contains("spaceAfterColon"));
    assertTrue(json.contains("4"));
  }

  @Test
  void unknown_rule_is_ignored() {
    var rules = List.of(new Rule("unknown-rule", "x", true, null));

    String json = builder.buildConfigJson(rules);

    assertEquals("{}", json);
  }
}
