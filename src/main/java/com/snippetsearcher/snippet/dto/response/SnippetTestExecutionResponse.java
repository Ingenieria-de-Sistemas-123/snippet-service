package com.snippetsearcher.snippet.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SnippetTestExecutionResponse(
    UUID testId,
    boolean passed,
    Integer exitCode,
    String output,
    String error,
    OffsetDateTime executedAt) {}
