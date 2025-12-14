package com.snippetsearcher.snippet.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.snippetsearcher.snippet.dto.response.FileTypeResponse;
import com.snippetsearcher.snippet.service.FileTypeService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FileTypeController.class)
@Import(FileTypeControllerTest.TestCfg.class)
class FileTypeControllerTest {

  @Autowired private MockMvc mvc;
  @Autowired private FileTypeService fileTypeService;

  @TestConfiguration
  static class TestCfg {
    @Bean
    FileTypeService fileTypeService() {
      return org.mockito.Mockito.mock(FileTypeService.class);
    }
  }

  @Test
  void getFileTypes_returnsList() throws Exception {
    when(fileTypeService.getFileTypes()).thenReturn(List.of(new FileTypeResponse("ps", ".ps")));

    mvc.perform(get("/api/file-types").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith("application/json"))
        .andExpect(jsonPath("$[0].language").value("ps"))
        .andExpect(jsonPath("$[0].extension").value(".ps"));
  }
}
