package com.snippetsearcher.snippet.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateSnippetTestRequest(
    @NotBlank(message = "El nombre del test es obligatorio.") String name,
    String description,
    String input,
    @NotBlank(message = "El output esperado es obligatorio.") String expectedOutput) {}
