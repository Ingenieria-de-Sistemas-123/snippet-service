package com.snippetsearcher.snippet.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSnippetRequest(
    @NotBlank @Size(max = 200) String name,
    @NotBlank @Size(max = 1000) String description,
    @NotBlank @Size(max = 50) String language,
    @NotBlank @Size(max = 20) String version) {}
