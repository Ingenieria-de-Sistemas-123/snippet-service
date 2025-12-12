package com.snippetsearcher.snippet.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippetsearcher.snippet.client.AssetClient;
import com.snippetsearcher.snippet.jobs.SnippetJobProducer;
import com.snippetsearcher.snippet.rules.Rule;
import com.snippetsearcher.snippet.rules.RulesFactory;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LintingRulesService {

  private static final Logger log = LoggerFactory.getLogger(LintingRulesService.class);

  private static final String RULES_CONTAINER = "rules";
  private static final String LINT_RULES_KEY = "lint-rules.json";

  private final AssetClient assetClient;
  private final ObjectMapper objectMapper;
  private final RulesFactory rulesFactory;
  private final SnippetJobProducer jobProducer;

  public LintingRulesService(
      AssetClient assetClient, ObjectMapper objectMapper, SnippetJobProducer jobProducer) {
    this.assetClient = assetClient;
    this.objectMapper = objectMapper;
    this.rulesFactory = new RulesFactory();
    this.jobProducer = jobProducer;
  }

  public List<Rule> getLintingRules() {
    try {
      byte[] data = assetClient.downloadSnippet(RULES_CONTAINER + "/" + LINT_RULES_KEY);
      String json = new String(data, StandardCharsets.UTF_8);
      return objectMapper.readValue(json, new TypeReference<List<Rule>>() {});
    } catch (Exception ex) {
      log.info("No hay reglas de linting configuradas, usando defaults: {}", ex.getMessage());
      return rulesFactory.getDefaultLintingRules();
    }
  }

  public List<Rule> updateLintingRules(List<Rule> rules, UUID adminId) {
    try {
      String json = objectMapper.writeValueAsString(rules);
      byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
      assetClient.uploadSnippet(RULES_CONTAINER, LINT_RULES_KEY, bytes, "application/json");
      log.info("Reglas de linting actualizadas");

      jobProducer.enqueueLintAll(adminId);

      return rules;
    } catch (Exception ex) {
      throw new IllegalStateException("No se pudieron actualizar las reglas de linting.", ex);
    }
  }
}
