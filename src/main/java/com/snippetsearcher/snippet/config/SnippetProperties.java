package com.snippetsearcher.snippet.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@ConfigurationProperties(prefix = "snippet")
public class SnippetProperties {

  private final List<FileType> fileTypes = new ArrayList<>();

    @Setter
    @Getter
    public static class FileType {
    private String language;
    private String extension;

    }
}
