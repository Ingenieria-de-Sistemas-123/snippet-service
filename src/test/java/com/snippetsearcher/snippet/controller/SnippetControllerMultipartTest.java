package com.snippetsearcher.snippet.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.snippetsearcher.snippet.dto.response.SnippetResponse;
import com.snippetsearcher.snippet.service.SnippetLanguageService;
import com.snippetsearcher.snippet.service.SnippetService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SnippetController.class)
@Import(SnippetControllerMultipartTest.TestCfg.class)
class SnippetControllerMultipartTest {

  @Autowired private MockMvc mvc;
  @Autowired private SnippetService snippetService;

  @TestConfiguration
  static class TestCfg {
    @Bean
    SnippetService snippetService() {
      return org.mockito.Mockito.mock(SnippetService.class);
    }

    @Bean
    SnippetLanguageService snippetLanguageService() {
      return org.mockito.Mockito.mock(SnippetLanguageService.class);
    }
  }

  @Test
  void createSnippet_returns200() throws Exception {
    when(snippetService.createSnippet(any(), any(), any()))
        .thenReturn(
            new SnippetResponse(
                UUID.randomUUID(),
                "n",
                "ps",
                "1",
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                List.of(),
                null,
                null));

    MockMultipartFile file =
        new MockMultipartFile("file", "a.ps", "text/plain", "print(1);".getBytes());

    MockMultipartFile req =
        new MockMultipartFile(
            "request",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            """
                        {"name":"n","language":"ps","version":"1"}
                        """
                .getBytes());

    mvc.perform(multipart("/api/snippets").file(file).file(req).with(jwt()))
        .andExpect(status().isOk());
  }
}
