package com.snippetsearcher.snippet.dto.response;

public record FormatSnippetResponse(
    boolean changed, String formatted, String content, String diagnostics) {}
