package com.snippetsearcher.snippet.dto.response;

import com.snippetsearcher.snippet.model.SnippetTest;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SnippetTestResponse(
    UUID id,
    String name,
    String description,
    String input,
    String expectedOutput,
    OffsetDateTime lastRunAt,
    Integer lastRunExitCode,
    String lastRunOutput,
    String lastRunError) {

  public static SnippetTestResponse fromEntity(SnippetTest test) {
    return new SnippetTestResponse(
        test.getId(),
        test.getName(),
        test.getDescription(),
        test.getInput(),
        test.getExpectedOutput(),
        test.getLastRunAt(),
        test.getLastRunExitCode(),
        test.getLastRunOutput(),
        test.getLastRunError());
  }
}
