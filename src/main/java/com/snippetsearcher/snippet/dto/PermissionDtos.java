package com.snippetsearcher.snippet.dto;

public class PermissionDtos {
    public record AuthorizeRequest(String action, String snippetId, String resourceOwnerSub) {}
    public record AuthorizeResponse(boolean allowed, String message) {}
}
