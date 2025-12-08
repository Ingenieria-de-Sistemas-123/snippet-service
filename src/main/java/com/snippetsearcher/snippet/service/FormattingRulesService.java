// src/main/java/com/snippetsearcher/snippet/service/FormattingRulesService.java
package com.snippetsearcher.snippet.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippetsearcher.snippet.client.AssetClient;
import com.snippetsearcher.snippet.jobs.SnippetJobProducer;
import com.snippetsearcher.snippet.rules.Rule;
import com.snippetsearcher.snippet.rules.RulesFactory;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * UC 11: habilitar / deshabilitar reglas de formatting. UC 12: al modificar reglas, disparar
 * formateo automático (via jobs).
 */
@Service
public class FormattingRulesService {

  private static final Logger log = LoggerFactory.getLogger(FormattingRulesService.class);

  private static final String RULES_CONTAINER = "rules";
  private static final String FORMAT_RULES_KEY = "format-rules.json";

  private final AssetClient assetClient;
  private final ObjectMapper objectMapper;
  private final RulesFactory rulesFactory;
  private final SnippetJobProducer jobProducer;

  public FormattingRulesService(
      AssetClient assetClient, ObjectMapper objectMapper, SnippetJobProducer jobProducer) {
    this.assetClient = assetClient;
    this.objectMapper = objectMapper;
    this.rulesFactory = new RulesFactory();
    this.jobProducer = jobProducer;
  }

  public List<Rule> getFormattingRules() {
    try {
      byte[] data = assetClient.downloadSnippet(RULES_CONTAINER + "/" + FORMAT_RULES_KEY);
      String json = new String(data, StandardCharsets.UTF_8);
      return objectMapper.readValue(json, new TypeReference<List<Rule>>() {});
    } catch (Exception ex) {
      // si no existe el asset o falla, devolvemos defaults
      log.info("No hay reglas de formatting configuradas, usando defaults: {}", ex.getMessage());
      return rulesFactory.getDefaultFormattingRules();
    }
  }

  /** Guarda las reglas y dispara el formateo masivo */
  public List<Rule> updateFormattingRules(List<Rule> rules, java.util.UUID adminId) {
    try {
      String json = objectMapper.writeValueAsString(rules);
      byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
      assetClient.uploadSnippet(RULES_CONTAINER, FORMAT_RULES_KEY, bytes, "application/json");
      log.info("Reglas de formatting actualizadas");

      jobProducer.enqueueFormatAll(adminId);

      return rules;
    } catch (Exception ex) {
      throw new IllegalStateException("No se pudieron actualizar las reglas de formatting.", ex);
    }
  }
}
