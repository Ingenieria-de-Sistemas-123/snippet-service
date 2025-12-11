package com.snippetsearcher.snippet.dto.response;

public record SnippetLintErrorResponse(String rule, Integer line, Integer column, String message) {}
