package com.snippetsearcher.snippet.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateSnippetTestRequest(
    @NotBlank(message = "El nombre del test es obligatorio.") String name,
    @NotBlank(message = "El script del test es obligatorio.") String script) {}
