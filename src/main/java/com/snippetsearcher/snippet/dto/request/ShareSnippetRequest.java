package com.snippetsearcher.snippet.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ShareSnippetRequest(@NotNull UUID userId) {}
