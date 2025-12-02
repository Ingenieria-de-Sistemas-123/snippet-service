package com.snippetsearcher.snippet.dto;

import java.util.List;

public class LanguageDtos {
  public record ValidateRequest(String language, String version, String content) {}
  public record ValidationError(String rule, int line, int col, String message) {}
  public record ValidateResponse(boolean valid, List<ValidationError> errors) {}

  public record FormatRequest(String language, String version, String content, Boolean check) {}
  public record FormatResponse(boolean changed, String formatted, String diagnostics) {}

  public record AnalyzeRequest(String language, String version, String content) {}
  public record AnalyzeIssue(String message) {}
  public record AnalyzeResponse(List<AnalyzeIssue> issues, String raw) {}

  public record ExecuteRequest(String language, String version, String content) {}
  public record ExecuteResponse(int exitCode, String stdout, String stderr) {}
}
