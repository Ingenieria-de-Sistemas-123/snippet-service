package com.snippetsearcher.snippet.dto;

import java.util.UUID;

public record PermissionUserDto(
        UUID id,
        String name,
        String email
) {}