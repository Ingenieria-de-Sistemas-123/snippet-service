package com.snippetsearcher.snippet.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.snippetsearcher.snippet.rules.Rule;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Builds the JSON configuration expected by the PrintScript formatter CLI from our domain rules.
 * Centralized here so every caller (manual format endpoint, batch worker, etc.) stays consistent.
 */
@Service
public class FormatterConfigBuilder {

  private static final Logger log = LoggerFactory.getLogger(FormatterConfigBuilder.class);
  private final ObjectMapper mapper = new ObjectMapper();

  public String buildConfigJson(List<Rule> rules) {
    ObjectNode root = mapper.createObjectNode();

    for (Rule rule : rules) {
      if (!rule.isActive()) continue;

      switch (rule.getId()) {
        case "spaceBeforeColon" -> root.put("spaceBeforeColon", true);
        case "spaceAfterColon" -> root.put("spaceAfterColon", true);
        case "spaceAroundEquals" -> root.put("spaceAroundEquals", true);
        case "spaceAroundOperators" -> root.put("spaceAroundOperators", true);
        case "lineJumpAfterSemicolon" -> root.put("lineJumpAfterSemicolon", true);
        case "singleSpaceSeparation" -> root.put("singleSpaceSeparation", true);
        case "indentSize" -> {
          Integer v = rule.getValue();
          if (v != null) {
            root.put("indentSize", v);
          }
        }
        default -> log.debug("Formatting rule without mapping to formatter JSON: {}", rule.getId());
      }
    }

    try {
      return mapper.writeValueAsString(root);
    } catch (Exception e) {
      throw new RuntimeException("Error building format config JSON: " + e.getMessage(), e);
    }
  }
}
