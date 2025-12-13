package com.snippetsearcher.snippet.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.snippetsearcher.snippet.client.language.LanguageClient;
import com.snippetsearcher.snippet.dto.LanguageDtos;
import com.snippetsearcher.snippet.dto.request.FormatSnippetRequest;
import com.snippetsearcher.snippet.dto.response.FormatSnippetResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SnippetLanguageServiceTest {

  @Mock private LanguageClient languageClient;
  @Mock private FormattingRulesService formattingRulesService;

  @Test
  void throwsWhenContentMissing() {
    SnippetLanguageService service =
        new SnippetLanguageService(languageClient, formattingRulesService);

    assertThrows(
        IllegalArgumentException.class,
        () -> service.formatSnippet(new FormatSnippetRequest("", "ps", "1.0", true)));
    verifyNoInteractions(languageClient, formattingRulesService);
  }

  @Test
  void returnsOriginalWhenLanguageMissing() {
    SnippetLanguageService service =
        new SnippetLanguageService(languageClient, formattingRulesService);

    FormatSnippetResponse response =
        service.formatSnippet(new FormatSnippetRequest("code", "", "1.0", false));

    assertFalse(response.changed());
    assertEquals("code", response.formatted());
    verifyNoInteractions(languageClient, formattingRulesService);
  }

  @Test
  void formatsUsingConfigJsonAndFallsBackWhenFormattedBlank() {
    when(formattingRulesService.buildFormatterConfigJsonFromStoredRules())
        .thenReturn("{\"cfg\":1}");
    when(languageClient.format(any()))
        .thenReturn(new LanguageDtos.FormatResponse(true, "", "diag"));

    SnippetLanguageService service =
        new SnippetLanguageService(languageClient, formattingRulesService);

    FormatSnippetRequest request = new FormatSnippetRequest("code", "ps", "1.0", true);
    FormatSnippetResponse response = service.formatSnippet(request);

    assertTrue(response.changed());
    assertEquals("code", response.formatted(), "blank formatted text should fall back to input");

    ArgumentCaptor<LanguageDtos.FormatRequest> captor =
        ArgumentCaptor.forClass(LanguageDtos.FormatRequest.class);
    verify(languageClient).format(captor.capture());
    assertEquals("{\"cfg\":1}", captor.getValue().configJson());
    assertEquals("ps", captor.getValue().language());
    assertEquals("1.0", captor.getValue().version());
  }

  @Test
  void wrapsLanguageClientErrors() {
    when(formattingRulesService.buildFormatterConfigJsonFromStoredRules()).thenReturn("{}");
    when(languageClient.format(any())).thenThrow(new IllegalStateException("boom"));

    SnippetLanguageService service =
        new SnippetLanguageService(languageClient, formattingRulesService);

    assertThrows(
        IllegalStateException.class,
        () -> service.formatSnippet(new FormatSnippetRequest("code", "ps", null, null)));
    verify(languageClient).format(any());
  }
}
