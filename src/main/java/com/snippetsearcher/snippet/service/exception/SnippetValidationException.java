package com.snippetsearcher.snippet.service.exception;

import com.snippetsearcher.snippet.dto.LanguageDtos;
import java.util.List;

public class SnippetValidationException extends RuntimeException {
  private final List<LanguageDtos.ValidationError> errors;

  public SnippetValidationException(String message, List<LanguageDtos.ValidationError> errors) {
    super(message);
    this.errors = errors;
  }

  public List<LanguageDtos.ValidationError> getErrors() {
    return errors;
  }
}

