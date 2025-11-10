package com.snippetsearcher.snippet.service.exception;

import java.util.UUID;

public class SnippetNotFoundException extends RuntimeException {
  public SnippetNotFoundException(UUID id) {
    super("Snippet not found: " + id);
  }
}

