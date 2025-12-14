package com.snippetsearcher.snippet.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.snippetsearcher.snippet.config.SnippetProperties;
import com.snippetsearcher.snippet.dto.response.FileTypeResponse;
import org.junit.jupiter.api.Test;

class FileTypeServiceTest {

  @Test
  void mapsConfiguredFileTypes() {
    SnippetProperties props = new SnippetProperties();
    SnippetProperties.FileType ft = new SnippetProperties.FileType();
    ft.setLanguage("ps");
    ft.setExtension(".prs");
    props.getFileTypes().add(ft);

    FileTypeService service = new FileTypeService(props);

    FileTypeResponse response = service.getFileTypes().getFirst();
    assertEquals("ps", response.language());
    assertEquals(".prs", response.extension());
  }

  @Test
  void maps_properties_to_response() {
    SnippetProperties props = new SnippetProperties();
    var ft = new SnippetProperties.FileType();
    ft.setLanguage("java");
    ft.setExtension("java");
    props.getFileTypes().add(ft);

    FileTypeService service = new FileTypeService(props);

    var res = service.getFileTypes();

    assertEquals(1, res.size());
    FileTypeResponse r = res.get(0);
    assertEquals("java", r.language());
    assertEquals("java", r.extension());
  }
}
