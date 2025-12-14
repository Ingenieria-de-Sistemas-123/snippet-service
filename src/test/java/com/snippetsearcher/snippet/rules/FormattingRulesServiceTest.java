package com.snippetsearcher.snippet.rules;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippetsearcher.snippet.client.AssetClient;
import com.snippetsearcher.snippet.jobs.SnippetJobProducer;
import com.snippetsearcher.snippet.service.FormatterConfigBuilder;
import com.snippetsearcher.snippet.service.FormattingRulesService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class FormattingRulesServiceTest {

  private AssetClient assetClient;
  private SnippetJobProducer jobProducer;
  private FormattingRulesService service;

  @BeforeEach
  void setup() {
    assetClient = mock(AssetClient.class);
    jobProducer = mock(SnippetJobProducer.class);

    service =
        new FormattingRulesService(
            assetClient, new ObjectMapper(), new FormatterConfigBuilder(), jobProducer);
  }

  @Test
  void getFormattingRules_whenNoAsset_returnsDefaults() {
    when(assetClient.downloadSnippet(anyString())).thenThrow(new RuntimeException("not found"));

    List<Rule> rules = service.getFormattingRules();

    assertFalse(rules.isEmpty());
  }

  @Test
  void updateFormattingRules_uploadsAsset_and_enqueuesJob() {
    List<Rule> rules = List.of(new Rule("x", "X", true, 2));
    UUID adminId = UUID.randomUUID();

    List<Rule> result = service.updateFormattingRules(rules, adminId);

    assertEquals(1, result.size());

    // Verifica que subió el asset (capturando bytes)
    ArgumentCaptor<byte[]> bytesCaptor = ArgumentCaptor.forClass(byte[].class);
    verify(assetClient)
        .uploadSnippet(
            eq("rules"), eq("format-rules.json"), bytesCaptor.capture(), eq("application/json"));

    String uploadedJson = new String(bytesCaptor.getValue(), StandardCharsets.UTF_8);
    assertTrue(uploadedJson.contains("\"id\":\"x\""));

    // Verifica que encoló el job
    verify(jobProducer).enqueueFormatAll(adminId);
  }
}
