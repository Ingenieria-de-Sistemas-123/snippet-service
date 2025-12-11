package com.snippetsearcher.snippet.dto;

import java.util.UUID;

public record SnippetPermissionDto(UUID snippetId, PermissionTypeDto type) {}
