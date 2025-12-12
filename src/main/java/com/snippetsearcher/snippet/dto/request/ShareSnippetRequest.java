package com.snippetsearcher.snippet.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ShareSnippetRequest(@NotNull @JsonProperty("userId") UUID userId) {}
