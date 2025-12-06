package com.snippetsearcher.snippet.dto.request;

import com.snippetsearcher.snippet.dto.PermissionTypeDto;
import java.util.UUID;

public record CreatePermissionRequestDto(UUID snippetId, UUID userId, PermissionTypeDto type) {}
