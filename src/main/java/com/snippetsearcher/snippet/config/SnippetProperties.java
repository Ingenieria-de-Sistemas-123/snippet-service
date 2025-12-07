package com.snippetsearcher.snippet.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "snippet")
public class SnippetProperties {

  private final List<FileType> fileTypes = new ArrayList<>();

  public List<FileType> getFileTypes() {
    return fileTypes;
  }

  public static class FileType {
    private String language;
    private String extension;

    public String getLanguage() {
      return language;
    }

    public void setLanguage(String language) {
      this.language = language;
    }

    public String getExtension() {
      return extension;
    }

    public void setExtension(String extension) {
      this.extension = extension;
    }
  }
}
