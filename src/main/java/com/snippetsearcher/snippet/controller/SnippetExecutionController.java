package com.snippetsearcher.snippet.controller;

import com.snippetsearcher.snippet.dto.LanguageDtos;
import com.snippetsearcher.snippet.service.SnippetService;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/snippets")
public class SnippetExecutionController {

    private final SnippetService snippetService;

    public SnippetExecutionController(SnippetService snippetService) {
        this.snippetService = snippetService;
    }

    public record ExecuteSnippetRequest(String input) {}

    public record ExecuteSnippetResponse(int exitCode, String stdout, String stderr) {}

    @PostMapping("/{id}/execute")
    public ExecuteSnippetResponse execute(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable("id") UUID snippetId,
            @RequestBody ExecuteSnippetRequest req) {

        LanguageDtos.ExecuteResponse res =
                snippetService.executeSnippet(jwt, snippetId, req.input());

        return new ExecuteSnippetResponse(res.exitCode(), res.stdout(), res.stderr());
    }
}
