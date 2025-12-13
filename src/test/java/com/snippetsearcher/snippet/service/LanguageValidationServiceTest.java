package com.snippetsearcher.snippet.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.snippetsearcher.snippet.dto.LanguageDtos;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@ExtendWith(MockitoExtension.class)
class LanguageValidationServiceTest {

  @Mock private RestClient restClient;

  @Test
  void validatesSnippetAgainstLanguageService() {
    RestClient mockClient = mock(RestClient.class, RETURNS_DEEP_STUBS);
    when(mockClient
            .post()
            .uri(anyString())
            .body(any(LanguageDtos.ValidateRequest.class))
            .retrieve()
            .body(LanguageDtos.ValidateResponse.class))
        .thenReturn(new LanguageDtos.ValidateResponse(true, List.of()));

    LanguageValidationService service = new LanguageValidationService(mockClient);

    LanguageDtos.ValidateResponse response = service.validate("ps", "1.0", "print(1);");

    assertTrue(response.valid());
  }

  @Test
  void wrapsHttpErrors() {
    RestClient mockClient = mock(RestClient.class, RETURNS_DEEP_STUBS);
    when(mockClient
            .post()
            .uri(anyString())
            .body(any(LanguageDtos.ValidateRequest.class))
            .retrieve()
            .body(LanguageDtos.ValidateResponse.class))
        .thenThrow(
            new RestClientResponseException(
                "bad",
                HttpStatus.BAD_REQUEST.value(),
                "400",
                null,
                new byte[0],
                StandardCharsets.UTF_8));

    LanguageValidationService service = new LanguageValidationService(mockClient);

    assertThrows(IllegalStateException.class, () -> service.validate("ps", "1.0", "print(1);"));
  }

  @Test
  void wrapsConnectivityErrors() {
    RestClient mockClient = mock(RestClient.class, RETURNS_DEEP_STUBS);
    when(mockClient
            .post()
            .uri(anyString())
            .body(any(LanguageDtos.ValidateRequest.class))
            .retrieve()
            .body(LanguageDtos.ValidateResponse.class))
        .thenThrow(new RestClientException("down"));

    LanguageValidationService service = new LanguageValidationService(mockClient);

    assertThrows(IllegalStateException.class, () -> service.validate("ps", "1.0", "print(1);"));
  }
}
