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
class LintingRulesServiceTest {

  @Mock private AssetClient assetClient;
  @Mock private SnippetJobProducer jobProducer;

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void returnsDefaultsWhenDownloadFails() {
    when(assetClient.downloadSnippet(any())).thenThrow(new IllegalStateException("missing"));

    LintingRulesService service = new LintingRulesService(assetClient, objectMapper, jobProducer);

    List<Rule> rules = service.getLintingRules();

    assertFalse(rules.isEmpty(), "defaults should be returned");
    verify(assetClient).downloadSnippet(any());
  }

  @Test
  void uploadsUpdatedRulesAndEnqueuesLintJob() throws Exception {
    LintingRulesService service = new LintingRulesService(assetClient, objectMapper, jobProducer);
    List<Rule> rules = List.of(new Rule("id", "name", true, 1));
    UUID adminId = UUID.randomUUID();

    List<Rule> response = service.updateLintingRules(rules, adminId);

    assertEquals(rules, response);

    ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);
    verify(assetClient)
        .uploadSnippet(
            eq("rules"), eq("lint-rules.json"), bytesCaptor.capture(), eq("application/json"));
    String json = new String(bytesCaptor.getValue(), StandardCharsets.UTF_8);
    assertTrue(json.contains("\"id\""));
    verify(jobProducer).enqueueLintAll(adminId);
  }
}
