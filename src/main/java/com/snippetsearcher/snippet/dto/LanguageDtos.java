package com.snippetsearcher.snippet.dto;

import java.util.List;

public class LanguageDtos {
  public record ValidateRequest(String language, String content) {}

  public record ValidationError(String rule, int line, int col, String message) {}

  public record ValidateResponse(boolean valid, List<ValidationError> errors) {}
}
