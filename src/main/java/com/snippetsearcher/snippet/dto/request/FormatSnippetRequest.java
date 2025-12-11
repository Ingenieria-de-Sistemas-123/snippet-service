package com.snippetsearcher.snippet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FormatSnippetRequest(
    @NotBlank String content,
    @Size(max = 50) String language,
    @Size(max = 20) String version,
    Boolean check) {}
