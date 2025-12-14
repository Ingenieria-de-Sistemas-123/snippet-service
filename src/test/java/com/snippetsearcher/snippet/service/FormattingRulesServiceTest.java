package com.snippetsearcher.snippet.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippetsearcher.snippet.client.AssetClient;
import com.snippetsearcher.snippet.jobs.SnippetJobProducer;
import com.snippetsearcher.snippet.rules.Rule;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FormattingRulesServiceTest {

  @Mock private AssetClient assetClient;
  @Mock private FormatterConfigBuilder formatterConfigBuilder;
  @Mock private SnippetJobProducer jobProducer;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void returnsDefaultsWhenNoStoredRules() {
    when(assetClient.downloadSnippet(any())).thenThrow(new IllegalStateException("missing"));

    FormattingRulesService service =
        new FormattingRulesService(assetClient, objectMapper, formatterConfigBuilder, jobProducer);

    List<Rule> rules = service.getFormattingRules();

    assertFalse(rules.isEmpty());
    verify(assetClient).downloadSnippet(any());
  }

  @Test
  void uploadsRulesAndTriggersFormatJob() throws Exception {
    FormattingRulesService service =
        new FormattingRulesService(assetClient, objectMapper, formatterConfigBuilder, jobProducer);
    List<Rule> rules = List.of(new Rule("indentSize", "indent", true, 4));
    UUID adminId = UUID.randomUUID();

    List<Rule> result = service.updateFormattingRules(rules, adminId);

    assertEquals(rules, result);
    ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);
    verify(assetClient)
        .uploadSnippet(
            eq("rules"), eq("format-rules.json"), bytesCaptor.capture(), eq("application/json"));

    String json = new String(bytesCaptor.getValue(), StandardCharsets.UTF_8);
    assertTrue(json.contains("indentSize"));
    verify(jobProducer).enqueueFormatAll(adminId);
  }

  @Test
  void fallsBackToEmptyJsonWhenBuilderFails() {
    when(formatterConfigBuilder.buildConfigJson(any())).thenThrow(new RuntimeException("boom"));
    when(assetClient.downloadSnippet(any())).thenReturn("[]".getBytes(StandardCharsets.UTF_8));

    FormattingRulesService service =
        new FormattingRulesService(assetClient, objectMapper, formatterConfigBuilder, jobProducer);

    String json = service.buildFormatterConfigJsonFromStoredRules();

    assertEquals("{}", json);
  }

  @Test
  void returns_defaults_when_asset_missing() {
    AssetClient asset = mock(AssetClient.class);
    ObjectMapper mapper = new ObjectMapper();
    FormatterConfigBuilder builder = mock(FormatterConfigBuilder.class);
    SnippetJobProducer jobs = mock(SnippetJobProducer.class);

    when(asset.downloadSnippet(any())).thenThrow(new RuntimeException("not found"));

    FormattingRulesService service = new FormattingRulesService(asset, mapper, builder, jobs);

    var rules = service.getFormattingRules();

    assertFalse(rules.isEmpty());
  }
}
