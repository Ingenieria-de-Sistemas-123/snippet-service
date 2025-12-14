package com.snippetsearcher.snippet.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.snippetsearcher.snippet.dto.response.*;
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
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SnippetController.class)
@Import(SnippetControllerTest.TestCfg.class)
class SnippetControllerTest {

  @Autowired private MockMvc mvc;
  @Autowired private SnippetService snippetService;
  @Autowired private SnippetLanguageService snippetLanguageService;

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
  void listSnippets_returns200() throws Exception {
    when(snippetService.listSnippets(any(), any()))
        .thenReturn(new ListSnippetsResponse(0, 10, 0, List.of()));

    mvc.perform(get("/api/snippets").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.page").value(0));
  }

  @Test
  void listSnippets_invalidQuery_returns400() throws Exception {
    mvc.perform(get("/api/snippets").with(jwt()).param("sort_dir", "nope"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getSnippet_returns200() throws Exception {
    UUID id = UUID.randomUUID();
    when(snippetService.getSnippet(any(), eq(id)))
        .thenReturn(
            new SnippetResponse(
                id, "n", "ps", "1", null, null, null, null, null, null, List.of(), List.of(), null,
                null));

    mvc.perform(get("/api/snippets/" + id).with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id.toString()));
  }

  @Test
  void deleteSnippet_returns204() throws Exception {
    UUID id = UUID.randomUUID();

    mvc.perform(delete("/api/snippets/" + id).with(jwt())).andExpect(status().isNoContent());
  }

  @Test
  void shareSnippet_returns200() throws Exception {
    UUID snippetId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    when(snippetService.shareSnippet(any(), eq(snippetId), any()))
        .thenReturn(
            new SnippetResponse(
                snippetId, "n", "ps", "1", null, null, null, null, null, null, List.of(), List.of(),
                null, null));

    mvc.perform(
            post("/api/snippets/" + snippetId + "/share")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                        {"userId":"%s"}
                                        """
                        .formatted(userId)))
        .andExpect(status().isOk());
  }

  @Test
  void getUsers_returns200() throws Exception {
    when(snippetService.getFriends(any())).thenReturn(List.of(new FriendsResponse("1", "n", "e")));

    mvc.perform(get("/api/snippets/users").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].email").value("e"));
  }
}
