package com.snippetsearcher.snippet.dto;

import java.util.UUID;

public record UserAccountDto(UUID id, String auth0Sub, String email, String name, String picture) {}
