package com.snippetsearcher.snippet;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippetsearcher.snippet.controller.*;
import com.snippetsearcher.snippet.dto.request.*;
import com.snippetsearcher.snippet.dto.response.*;
import com.snippetsearcher.snippet.model.RuleType;
import com.snippetsearcher.snippet.rules.Rule;
import com.snippetsearcher.snippet.service.*;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class SnippetHttpE2ETest {

  private MockMvc mvc;
  private ObjectMapper mapper;
  private SnippetService snippetService;
  private SnippetLanguageService snippetLanguageService;
  private SnippetTestService snippetTestService;
  private FileTypeService fileTypeService;
  private RuleService ruleService;
  private FormattingRulesService formattingRulesService;
  private LintingRulesService lintingRulesService;

  @BeforeEach
  void setup() {
    mapper = new ObjectMapper();

    snippetService = Mockito.mock(SnippetService.class);
    snippetLanguageService = Mockito.mock(SnippetLanguageService.class);
    snippetTestService = Mockito.mock(SnippetTestService.class);
    fileTypeService = Mockito.mock(FileTypeService.class);
    ruleService = Mockito.mock(RuleService.class);
    formattingRulesService = Mockito.mock(FormattingRulesService.class);
    lintingRulesService = Mockito.mock(LintingRulesService.class);

    SnippetController snippetController =
        new SnippetController(snippetService, snippetLanguageService);
    SnippetExecutionController execController = new SnippetExecutionController(snippetService);
    SnippetTestController testController = new SnippetTestController(snippetTestService);
    FileTypeController fileTypeController = new FileTypeController(fileTypeService);
    RuleController deprecatedRuleController = new RuleController(ruleService);
    RulesController rulesController =
        new RulesController(formattingRulesService, lintingRulesService, snippetService);
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();

    mvc =
        MockMvcBuilders.standaloneSetup(
                snippetController,
                execController,
                testController,
                fileTypeController,
                deprecatedRuleController,
                rulesController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(validator)
            .build();
  }

  // ---------------------------
  // SnippetController "E2E"
  // ---------------------------

  @Test
  void listSnippets_invalidQuery_returns400_mappedByGlobalHandler() throws Exception {
    // El controller atrapa IllegalArgumentException y la convierte en ResponseStatusException(400).
    mvc.perform(get("/api/snippets").param("sort_dir", "nope")).andExpect(status().isBadRequest());
  }

  @Test
  void shareSnippet_requiresUserId_validation_400() throws Exception {
    UUID snippetId = UUID.randomUUID();

    ShareSnippetRequest invalid = new ShareSnippetRequest(null);

    mvc.perform(
            post("/api/snippets/" + snippetId + "/share")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(invalid)))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------
  // SnippetTestController "E2E"
  // ---------------------------

  @Test
  void createSnippetTest_invalidBody_returns400_validation() throws Exception {
    UUID snippetId = UUID.randomUUID();

    // name en blanco => @NotBlank
    mvc.perform(
            post("/api/snippets/tests/" + snippetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"name":"  ","script":"print(1);"}
                    """))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------
  // FileTypes + Rules endpoints "E2E"
  // ---------------------------

  @Test
  void fileTypes_returnsList() throws Exception {
    when(fileTypeService.getFileTypes())
        .thenReturn(
            List.of(new FileTypeResponse("ps", ".ps"), new FileTypeResponse("java", ".java")));

    mvc.perform(get("/api/file-types"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].language").value("ps"))
        .andExpect(jsonPath("$[0].extension").value(".ps"));
  }

  @Test
  void deprecatedRuleController_getFormat_getLint() throws Exception {
    when(ruleService.getRules(RuleType.FORMAT))
        .thenReturn(List.of(new RuleResponse("spaceAfterColon", "Space after ':'", true, null)));

    when(ruleService.getRules(RuleType.LINT))
        .thenReturn(
            List.of(
                new RuleResponse("noTrailingWhitespace", "No trailing whitespace", true, null)));

    mvc.perform(get("/api/rules/format"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("spaceAfterColon"));

    mvc.perform(get("/api/rules/lint"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("noTrailingWhitespace"));
  }

  @Test
  void deprecatedRuleController_updateRules_validBody_returns200() throws Exception {
    when(ruleService.replaceRules(eq(RuleType.FORMAT), anyList()))
        .thenAnswer(
            inv -> {
              @SuppressWarnings("unchecked")
              List<RuleRequest> reqs = (List<RuleRequest>) inv.getArgument(1);
              return reqs.stream()
                  .map(r -> new RuleResponse(r.id(), r.name(), r.isActive(), r.value()))
                  .toList();
            });

    String body =
        """
                [{"id":"spaceAfterColon","name":"Espacio después de ':'","isActive":true,"value":null}]
                """;

    mvc.perform(put("/api/rules/format").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("spaceAfterColon"))
        .andExpect(jsonPath("$[0].isActive").value(true));
  }

  @Test
  void deprecatedRuleController_updateRules_invalidBody_returns400_validation() throws Exception {
    // id en blanco => @NotBlank
    String invalid =
        """
                [{"id":"  ","name":"ok","isActive":true,"value":2}]
                """;

    mvc.perform(put("/api/rules/lint").contentType(MediaType.APPLICATION_JSON).content(invalid))
        .andExpect(status().isBadRequest());
  }

  // ---------------------------
  // RulesController endpoints (nuevo)
  // ---------------------------

  @Test
  void rulesController_getFormattingRules_returns200() throws Exception {
    when(formattingRulesService.getFormattingRules())
        .thenReturn(List.of(new Rule("spaceAfterColon", "Space after ':'", true, null)));

    mvc.perform(get("/api/rules/formatting"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("spaceAfterColon"));
  }

  @Test
  void rulesController_getLintingRules_returns200() throws Exception {
    when(lintingRulesService.getLintingRules())
        .thenReturn(
            List.of(new Rule("noTrailingWhitespace", "No trailing whitespace", true, null)));

    mvc.perform(get("/api/rules/linting"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value("noTrailingWhitespace"));
  }

  @Test
  void formatSnippet_endpoint_returnsServiceResponse() throws Exception {
    when(snippetLanguageService.formatSnippet(any(FormatSnippetRequest.class)))
        .thenReturn(new FormatSnippetResponse(true, "a b", "a  b", "diag"));

    mvc.perform(
            post("/api/snippets/format")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"content":"a  b","language":"ps","version":"1.0","check":false}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.changed").value(true))
        .andExpect(jsonPath("$.formatted").value("a b"))
        .andExpect(jsonPath("$.content").value("a  b"));
  }
}
