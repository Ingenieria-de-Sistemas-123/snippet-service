package com.snippetsearcher.snippet.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.service.SnippetService;
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
import org.mockito.Mockito;

@WebMvcTest(SnippetController.class)
@Import(SnippetControllerWebMvcTest.MvcTestConfig.class)
class SnippetControllerWebMvcTest {

  @TestConfiguration
  static class MvcTestConfig {
    @Bean
    SnippetService service() {
      return Mockito.mock(SnippetService.class);
    }
  }

  @Autowired MockMvc mvc;
  @Autowired SnippetService service;
  @Autowired ObjectMapper mapper;

  @Test
  void createSnippetJson_ok() throws Exception {
    var saved = Snippet.of("n","d","printscript","1.0","print 1");
    given(service.create(eq("n"), eq("d"), eq("printscript"), eq("1.0"), eq("print 1"))).willReturn(saved);
    var body = mapper.writeValueAsString(new SnippetController.CreateSnippet("n","d","printscript","1.0","print 1"));
    mvc.perform(post("/snippets").contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("n"))
        .andExpect(jsonPath("$.language").value("printscript"));
  }

  @Test
  void createSnippetMultipart_ok() throws Exception {
    var saved = Snippet.of("n","d","printscript","1.0","print 1");
    given(service.create(eq("n"), eq("d"), eq("printscript"), eq("1.0"), eq("print 1"))).willReturn(saved);
    MockMultipartFile file = new MockMultipartFile("file","a.ps","text/plain","print 1".getBytes());
    mvc.perform(multipart("/snippets")
            .file(file)
            .param("name","n")
            .param("description","d")
            .param("language","printscript")
            .param("version","1.0"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("n"))
        .andExpect(jsonPath("$.language").value("printscript"));
  }

  @Test
  void updateSnippetJson_ok() throws Exception {
    UUID id = UUID.randomUUID();
    var updated = Snippet.of("n2","d2","printscript","1.0","print 2");
    given(service.update(eq(id), eq("n2"), eq("d2"), eq("printscript"), eq("1.0"), eq("print 2"))).willReturn(updated);
    var body = mapper.writeValueAsString(new SnippetController.UpdateSnippet("n2","d2","printscript","1.0","print 2"));
    mvc.perform(put("/snippets/"+id).contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.description").value("d2"))
        .andExpect(jsonPath("$.content").value("print 2"));
  }

  @Test
  void updateSnippetMultipart_ok() throws Exception {
    UUID id = UUID.randomUUID();
    var updated = Snippet.of("n2","d2","printscript","1.0","print 2");
    given(service.update(eq(id), eq("n2"), eq("d2"), eq("printscript"), eq("1.0"), eq("print 2"))).willReturn(updated);
    MockMultipartFile file = new MockMultipartFile("file","a.ps","text/plain","print 2".getBytes());
    mvc.perform(multipart("/snippets/"+id)
            .file(file)
            .param("name","n2")
            .param("description","d2")
            .param("language","printscript")
            .param("version","1.0")
            .with(req -> { req.setMethod("PUT"); return req; }))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.description").value("d2"))
        .andExpect(jsonPath("$.content").value("print 2"));
  }
}
