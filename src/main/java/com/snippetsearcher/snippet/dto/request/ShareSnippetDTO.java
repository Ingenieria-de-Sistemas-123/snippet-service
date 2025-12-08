package com.snippetsearcher.snippet.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ShareSnippetDTO(
        @NotBlank String friendId
) { }
