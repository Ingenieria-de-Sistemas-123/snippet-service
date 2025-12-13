package com.snippetsearcher.snippet.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.snippetsearcher.snippet.client.language.LanguageClient;
import com.snippetsearcher.snippet.dto.LanguageDtos;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class LanguageClientTest {

  @Test
  void validate_callsValidateEndpointWithJson() {
    RestClient http = mock(RestClient.class);

    RestClient.RequestBodyUriSpec post = mock(RestClient.RequestBodyUriSpec.class);
    RestClient.RequestBodySpec withContentType = mock(RestClient.RequestBodySpec.class);
    RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

    LanguageDtos.ValidateRequest req = new LanguageDtos.ValidateRequest("ps", "1.0", "print(1);");
    LanguageDtos.ValidateResponse expected = new LanguageDtos.ValidateResponse(true, List.of());

    when(http.post()).thenReturn(post);
    when(post.uri("/validate")).thenReturn(withContentType);
    when(withContentType.contentType(APPLICATION_JSON)).thenReturn(withContentType);
    when(withContentType.body(req)).thenReturn(withContentType);
    when(withContentType.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(LanguageDtos.ValidateResponse.class)).thenReturn(expected);

    LanguageClient client = new LanguageClient(http);

    LanguageDtos.ValidateResponse res = client.validate(req);

    assertTrue(res.valid());
    verify(post, times(1)).uri("/validate");
    verify(withContentType, times(1)).contentType(APPLICATION_JSON);
    verify(withContentType, times(1)).body(req);
    verify(responseSpec, times(1)).body(LanguageDtos.ValidateResponse.class);
    verifyNoMoreInteractions(responseSpec);
  }

  @Test
  void format_callsFormatEndpointWithJson() {
    RestClient http = mock(RestClient.class);

    RestClient.RequestBodyUriSpec post = mock(RestClient.RequestBodyUriSpec.class);
    RestClient.RequestBodySpec withContentType = mock(RestClient.RequestBodySpec.class);
    RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

    LanguageDtos.FormatRequest req =
        new LanguageDtos.FormatRequest("ps", "1.0", "print(1);", false, "{\"x\":1}");
    LanguageDtos.FormatResponse expected = new LanguageDtos.FormatResponse(true, "fmt", "diag");

    when(http.post()).thenReturn(post);
    when(post.uri("/format")).thenReturn(withContentType);
    when(withContentType.contentType(APPLICATION_JSON)).thenReturn(withContentType);
    when(withContentType.body(req)).thenReturn(withContentType);
    when(withContentType.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(LanguageDtos.FormatResponse.class)).thenReturn(expected);

    LanguageClient client = new LanguageClient(http);

    LanguageDtos.FormatResponse res = client.format(req);

    assertTrue(res.changed());
    assertEquals("fmt", res.formatted());
    verify(post, times(1)).uri("/format");
    verify(withContentType, times(1)).contentType(APPLICATION_JSON);
    verify(withContentType, times(1)).body(req);
    verify(responseSpec, times(1)).body(LanguageDtos.FormatResponse.class);
  }

  @Test
  void analyze_callsAnalyzeEndpointWithJson() {
    RestClient http = mock(RestClient.class);

    RestClient.RequestBodyUriSpec post = mock(RestClient.RequestBodyUriSpec.class);
    RestClient.RequestBodySpec withContentType = mock(RestClient.RequestBodySpec.class);
    RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

    LanguageDtos.AnalyzeRequest req = new LanguageDtos.AnalyzeRequest("ps", "1.0", "print(1);");
    LanguageDtos.AnalyzeResponse expected = new LanguageDtos.AnalyzeResponse(List.of(), "{raw}");

    when(http.post()).thenReturn(post);
    when(post.uri("/analyze")).thenReturn(withContentType);
    when(withContentType.contentType(APPLICATION_JSON)).thenReturn(withContentType);
    when(withContentType.body(req)).thenReturn(withContentType);
    when(withContentType.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(LanguageDtos.AnalyzeResponse.class)).thenReturn(expected);

    LanguageClient client = new LanguageClient(http);

    LanguageDtos.AnalyzeResponse res = client.analyze(req);

    assertEquals("{raw}", res.raw());
    verify(post, times(1)).uri("/analyze");
    verify(withContentType, times(1)).contentType(APPLICATION_JSON);
    verify(withContentType, times(1)).body(req);
    verify(responseSpec, times(1)).body(LanguageDtos.AnalyzeResponse.class);
  }

  @Test
  void execute_callsExecuteEndpointWithJson() {
    RestClient http = mock(RestClient.class);

    RestClient.RequestBodyUriSpec post = mock(RestClient.RequestBodyUriSpec.class);
    RestClient.RequestBodySpec withContentType = mock(RestClient.RequestBodySpec.class);
    RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

    LanguageDtos.ExecuteRequest req =
        new LanguageDtos.ExecuteRequest("ps", "1.0", "print(input);", "123");
    LanguageDtos.ExecuteResponse expected = new LanguageDtos.ExecuteResponse(0, "out", "");

    when(http.post()).thenReturn(post);
    when(post.uri("/execute")).thenReturn(withContentType);
    when(withContentType.contentType(APPLICATION_JSON)).thenReturn(withContentType);
    when(withContentType.body(req)).thenReturn(withContentType);
    when(withContentType.retrieve()).thenReturn(responseSpec);
    when(responseSpec.body(LanguageDtos.ExecuteResponse.class)).thenReturn(expected);

    LanguageClient client = new LanguageClient(http);

    LanguageDtos.ExecuteResponse res = client.execute(req);

    assertEquals(0, res.exitCode());
    assertEquals("out", res.stdout());
    verify(post, times(1)).uri("/execute");
    verify(withContentType, times(1)).contentType(APPLICATION_JSON);
    verify(withContentType, times(1)).body(req);
    verify(responseSpec, times(1)).body(LanguageDtos.ExecuteResponse.class);
  }
}
