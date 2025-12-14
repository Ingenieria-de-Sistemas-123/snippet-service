package com.snippetsearcher.snippet.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.snippetsearcher.snippet.dto.LanguageDtos;
import com.snippetsearcher.snippet.service.SnippetService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SnippetExecutionController.class)
@Import(SnippetExecutionControllerTest.TestCfg.class)
class SnippetExecutionControllerTest {

  @Autowired private MockMvc mvc;
  @Autowired private SnippetService snippetService;

  @TestConfiguration
  static class TestCfg {
    @Bean
    SnippetService snippetService() {
      return org.mockito.Mockito.mock(SnippetService.class);
    }
  }

  @Test
  void executeSnippet_returns200() throws Exception {
    UUID id = UUID.randomUUID();

    when(snippetService.executeSnippet(any(), eq(id), eq("in")))
        .thenReturn(new LanguageDtos.ExecuteResponse(0, "out", ""));

    mvc.perform(
            post("/api/snippets/" + id + "/execute")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                        {"input":"in"}
                                        """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.exitCode").value(0))
        .andExpect(jsonPath("$.stdout").value("out"));
  }
}
