package com.snippetsearcher.snippet.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippetsearcher.snippet.dto.LanguageDtos;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.repository.SnippetRepository;
import com.snippetsearcher.snippet.service.LanguageValidationService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

@WebMvcTest(SnippetController.class)
class SnippetControllerWebMvcTest {

  @Autowired MockMvc mvc;
  @MockBean LanguageValidationService validator;
  @MockBean SnippetRepository repo;
  @Autowired ObjectMapper mapper;

  @Test
  void createSnippetJson_ok() throws Exception {
    var validateResp = new LanguageDtos.ValidateResponse(true, List.of());
    given(validator.validate(eq("printscript"), eq("1.0"), eq("print 1"))).willReturn(validateResp);
    given(repo.save(any(Snippet.class))).willAnswer(inv -> inv.getArgument(0));
    var body = mapper.writeValueAsString(new SnippetController.CreateSnippet("n","d","printscript","1.0","print 1"));
    mvc.perform(post("/snippets").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("n"))
        .andExpect(jsonPath("$.language").value("printscript"));
  }

  @Test
  void createSnippetMultipart_ok() throws Exception {
    var validateResp = new LanguageDtos.ValidateResponse(true, List.of());
    given(validator.validate(eq("printscript"), eq("1.0"), eq("print 1"))).willReturn(validateResp);
    given(repo.save(any(Snippet.class))).willAnswer(inv -> inv.getArgument(0));
    MockMultipartFile file = new MockMultipartFile("file","a.ps","text/plain","print 1".getBytes());
    mvc.perform(multipart("/snippets")
            .file(file)
            .param("name","n")
            .param("description","d")
            .param("language","printscript")
            .param("version","1.0"))
        .andExpect(status().isOk());
  }

  @Test
  void updateSnippetJson_ok() throws Exception {
    UUID id = UUID.randomUUID();
    var existing = Snippet.of("n","d","printscript","1.0","print 1");
    existing.setId(id);
    given(repo.findById(eq(id))).willReturn(Optional.of(existing));
    var validateResp = new LanguageDtos.ValidateResponse(true, List.of());
    given(validator.validate(eq("printscript"), eq("1.0"), eq("print 2"))).willReturn(validateResp);
    given(repo.save(any(Snippet.class))).willAnswer(inv -> inv.getArgument(0));
    var body = mapper.writeValueAsString(new SnippetController.UpdateSnippet("n2","d2","printscript","1.0","print 2"));
    mvc.perform(put("/snippets/"+id).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.description").value("d2"))
        .andExpect(jsonPath("$.content").value("print 2"));
  }

  @Test
  void updateSnippetMultipart_notFound() throws Exception {
    UUID id = UUID.randomUUID();
    given(repo.findById(eq(id))).willReturn(Optional.empty());
    MockMultipartFile file = new MockMultipartFile("file","a.ps","text/plain","print 1".getBytes());
    mvc.perform(multipart("/snippets/"+id)
            .file(file)
            .param("name","n")
            .param("language","printscript")
            .param("version","1.0")
            .with(req -> { req.setMethod("PUT"); return req; }))
        .andExpect(status().isNotFound());
  }

  @Test
  void createSnippetJson_validationErrors() throws Exception {
    var validateResp = new LanguageDtos.ValidateResponse(false, List.of(new LanguageDtos.ValidationError("RULE",1,1,"msg")));
    given(validator.validate(eq("printscript"), eq("1.0"), eq("print 1"))).willReturn(validateResp);
    var body = mapper.writeValueAsString(new SnippetController.CreateSnippet("n","d","printscript","1.0","print 1"));
    mvc.perform(post("/snippets").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.valid").value(false));
  }
}
