package com.snippetsearcher.snippet.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RuleRequest(
    @NotBlank String id, @NotBlank String name, boolean isActive, Integer value) {}
