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
    String json =
        builder.buildConfigJson(List.of(new Rule("indentSize", "indent", true, 4)));

    JsonNode node = mapper.readTree(json);
    assertEquals(4, node.get("indentSize").asInt());
  }
}
