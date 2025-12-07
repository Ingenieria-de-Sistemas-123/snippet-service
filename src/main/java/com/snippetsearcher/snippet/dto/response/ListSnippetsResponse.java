package com.snippetsearcher.snippet.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ListSnippetsResponse(
    int page, @JsonProperty("page_size") int pageSize, @JsonProperty("count") long count, List<SnippetListItemResponse> snippets) {}
