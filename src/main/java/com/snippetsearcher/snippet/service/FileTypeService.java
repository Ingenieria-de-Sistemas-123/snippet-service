package com.snippetsearcher.snippet.service;

import com.snippetsearcher.snippet.config.SnippetProperties;
import com.snippetsearcher.snippet.dto.response.FileTypeResponse;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class FileTypeService {

  private final SnippetProperties snippetProperties;

  public FileTypeService(SnippetProperties snippetProperties) {
    this.snippetProperties = snippetProperties;
  }

  public List<FileTypeResponse> getFileTypes() {
    return snippetProperties.getFileTypes().stream()
        .map(ft -> new FileTypeResponse(ft.getLanguage(), ft.getExtension()))
        .collect(Collectors.toList());
  }
}
