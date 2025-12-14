package com.snippetsearcher.snippet.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.snippetsearcher.snippet.client.PermissionClient;
import com.snippetsearcher.snippet.dto.UserAccountDto;
import com.snippetsearcher.snippet.jobs.SnippetJobProducer;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SnippetAdminController.class)
@Import(SnippetAdminControllerTest.TestCfg.class)
class SnippetAdminControllerTest {

  @Autowired private MockMvc mvc;
  @Autowired private PermissionClient permissionClient;
  @Autowired private SnippetJobProducer jobProducer;

  @TestConfiguration
  static class TestCfg {
    @Bean
    PermissionClient permissionClient() {
      return org.mockito.Mockito.mock(PermissionClient.class);
    }

    @Bean
    SnippetJobProducer snippetJobProducer() {
      return org.mockito.Mockito.mock(SnippetJobProducer.class);
    }
  }

  @Test
  void formatAllSnippets_returns202() throws Exception {
    UserAccountDto user = new UserAccountDto(UUID.randomUUID(), "sub", "e", "n", null);

    when(permissionClient.ensureUser(any())).thenReturn(user);
    doNothing().when(jobProducer).enqueueFormatAll(user.id());

    mvc.perform(post("/api/admin/snippets/format").with(jwt())).andExpect(status().isAccepted());
  }

  @Test
  void lintAllSnippets_returns202() throws Exception {
    UserAccountDto user = new UserAccountDto(UUID.randomUUID(), "sub", "e", "n", null);

    when(permissionClient.ensureUser(any())).thenReturn(user);
    doNothing().when(jobProducer).enqueueLintAll(user.id());

    mvc.perform(post("/api/admin/snippets/lint").with(jwt())).andExpect(status().isAccepted());
  }
}
