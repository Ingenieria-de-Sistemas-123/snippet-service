package com.snippetsearcher.snippet.rules;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippetsearcher.snippet.client.AssetClient;
import com.snippetsearcher.snippet.jobs.SnippetJobProducer;
import com.snippetsearcher.snippet.service.LintingRulesService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class LintingRulesServiceTest {

  private AssetClient assetClient;
  private SnippetJobProducer jobProducer;
  private LintingRulesService service;

  @BeforeEach
  void setup() {
    assetClient = mock(AssetClient.class);
    jobProducer = mock(SnippetJobProducer.class);
    service = new LintingRulesService(assetClient, new ObjectMapper(), jobProducer);
  }

  @Test
  void getLintingRules_whenNoAsset_returnsDefaults() {
    when(assetClient.downloadSnippet(anyString())).thenThrow(new RuntimeException("not found"));

    List<Rule> rules = service.getLintingRules();

    assertFalse(rules.isEmpty());
  }

  @Test
  void updateLintingRules_uploadsAsset_and_enqueuesJob() {
    List<Rule> rules = List.of(new Rule("x", "X", true, null));
    UUID adminId = UUID.randomUUID();

    List<Rule> result = service.updateLintingRules(rules, adminId);

    assertEquals(1, result.size());

    ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);
    verify(assetClient)
        .uploadSnippet(
            eq("rules"), eq("lint-rules.json"), bytesCaptor.capture(), eq("application/json"));

    String uploadedJson = new String(bytesCaptor.getValue(), StandardCharsets.UTF_8);
    assertTrue(uploadedJson.contains("\"id\":\"x\""));

    verify(jobProducer).enqueueLintAll(adminId);
  }
}
