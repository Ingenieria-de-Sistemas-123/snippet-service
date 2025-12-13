package com.snippetsearcher.snippet.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.snippetsearcher.snippet.dto.response.RuleResponse;
import com.snippetsearcher.snippet.model.RuleType;
import com.snippetsearcher.snippet.service.RuleService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RuleController.class)
@Import(RuleControllerTest.TestCfg.class)
class RuleControllerTest {

  @Autowired private MockMvc mvc;
  @Autowired private RuleService ruleService;

  @TestConfiguration
  static class TestCfg {
    @Bean
    RuleService ruleService() {
      return org.mockito.Mockito.mock(RuleService.class);
    }
  }

  @Test
  void getFormatRules_returnsList() throws Exception {
    when(ruleService.getRules(eq(RuleType.FORMAT)))
        .thenReturn(List.of(new RuleResponse("r1", "Rule 1", true, 2)));

    mvc.perform(get("/api/rules/format").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("r1"));
  }

  @Test
  void getLintRules_returnsList() throws Exception {
    when(ruleService.getRules(eq(RuleType.LINT)))
        .thenReturn(List.of(new RuleResponse("l1", "Lint 1", false, null)));

    mvc.perform(get("/api/rules/lint").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("l1"));
  }

  @Test
  void updateFormatRules_validBody_returns200() throws Exception {
    when(ruleService.replaceRules(eq(RuleType.FORMAT), org.mockito.ArgumentMatchers.anyList()))
        .thenReturn(List.of(new RuleResponse("x", "y", true, 2)));

    mvc.perform(
            put("/api/rules/format")
                .with(jwt())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                        [{"id":"x","name":"y","isActive":true,"value":2}]
                                        """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("x"));
  }

  @Test
  void updateLintRules_invalidBody_returns400() throws Exception {
    // id en blanco => @NotBlank debería fallar
    mvc.perform(
            put("/api/rules/lint")
                .with(jwt())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                        [{"id":" ","name":"ok","isActive":true,"value":2}]
                                        """))
        .andExpect(status().isBadRequest());
  }
}
