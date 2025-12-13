package com.snippetsearcher.snippet.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.snippetsearcher.snippet.dto.response.SnippetTestExecutionResponse;
import com.snippetsearcher.snippet.dto.response.SnippetTestResponse;
import com.snippetsearcher.snippet.service.SnippetTestService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SnippetTestController.class)
@Import(SnippetTestControllerTest.TestCfg.class)
class SnippetTestControllerTest {

  @Autowired private MockMvc mvc;
  @Autowired private SnippetTestService snippetTestService;

  @TestConfiguration
  static class TestCfg {
    @Bean
    SnippetTestService snippetTestService() {
      return org.mockito.Mockito.mock(SnippetTestService.class);
    }
  }

  @Test
  void listSnippetTests_returns200() throws Exception {
    UUID snippetId = UUID.randomUUID();
    when(snippetTestService.listSnippetTests(any(), eq(snippetId))).thenReturn(List.of());

    mvc.perform(get("/api/snippets/tests/" + snippetId).with(jwt())).andExpect(status().isOk());
  }

  @Test
  void createSnippetTest_returns201() throws Exception {
    UUID snippetId = UUID.randomUUID();
    SnippetTestResponse resp =
        new SnippetTestResponse(UUID.randomUUID(), "t", "d", null, null, null, null);

    when(snippetTestService.createSnippetTest(any(), eq(snippetId), any())).thenReturn(resp);

    mvc.perform(
            post("/api/snippets/tests/" + snippetId)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                        {"name":"Test 1","script":"print(1);"}
                                        """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.name").value("t"));
  }

  @Test
  void createSnippetTest_invalidBody_returns400() throws Exception {
    UUID snippetId = UUID.randomUUID();

    // name blank -> @NotBlank => 400
    mvc.perform(
            post("/api/snippets/tests/" + snippetId)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                        {"name":" ","script":"print(1);"}
                                        """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void updateSnippetTest_returns200() throws Exception {
    UUID snippetId = UUID.randomUUID();
    UUID testId = UUID.randomUUID();
    SnippetTestResponse resp = new SnippetTestResponse(testId, "t", "d", null, null, null, null);

    when(snippetTestService.updateSnippetTest(any(), eq(snippetId), eq(testId), any()))
        .thenReturn(resp);

    mvc.perform(
            put("/api/snippets/tests/" + snippetId + "/" + testId)
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                        {"name":"Test 2","script":"print(2);"}
                                        """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").exists());
  }

  @Test
  void deleteSnippetTest_returns204() throws Exception {
    UUID snippetId = UUID.randomUUID();
    UUID testId = UUID.randomUUID();

    mvc.perform(delete("/api/snippets/tests/" + snippetId + "/" + testId).with(jwt()))
        .andExpect(status().isNoContent());
  }

  @Test
  void executeSnippetTest_returns200() throws Exception {
    UUID snippetId = UUID.randomUUID();
    UUID testId = UUID.randomUUID();

    when(snippetTestService.executeSnippetTest(any(), eq(snippetId), eq(testId)))
        .thenReturn(
            new SnippetTestExecutionResponse(
                testId, true, 0, "out", "", OffsetDateTime.parse("2025-01-01T00:00:00Z")));

    mvc.perform(post("/api/snippets/tests/" + snippetId + "/" + testId + "/execute").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.passed").value(true))
        .andExpect(jsonPath("$.exitCode").value(0));
  }
}
