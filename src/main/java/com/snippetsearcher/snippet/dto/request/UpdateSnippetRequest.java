package com.snippetsearcher.snippet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateSnippetRequest(
    @NotBlank @Size(max = 200) String name,
    @Size(max = 1000) String description,
    @NotBlank @Size(max = 50) String language,
    @Size(max = 20) String version) {}
