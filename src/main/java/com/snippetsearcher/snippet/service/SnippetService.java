package com.snippetsearcher.snippet.service;

import com.snippetsearcher.snippet.dto.LanguageDtos;
import com.snippetsearcher.snippet.language.LanguageClient;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.repository.SnippetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SnippetService {
    private final LanguageClient lang;
    private final SnippetRepository repo;

    public SnippetService(LanguageClient lang, SnippetRepository repo) {
        this.lang = lang;
        this.repo = repo;
    }

    public Snippet create(String name, String language, String versionOrNull, String content) {
        String version = (versionOrNull == null || versionOrNull.isBlank()) ? "1.0" : versionOrNull;

        var vRes = lang.validate(new LanguageDtos.ValidateRequest(language, version, content));
        if (vRes == null || !vRes.valid()) {
            throw new IllegalArgumentException("Invalid source: " + (vRes == null ? "unknown" : vRes.errors()));
        }

        String finalContent = content;
        var fRes = lang.format(new LanguageDtos.FormatRequest(language, version, content, false));
        if (fRes != null && fRes.formatted() != null && !fRes.formatted().isBlank()) {
            finalContent = fRes.formatted();
        }

        return repo.save(Snippet.of(name, language, finalContent));
    }

    public LanguageDtos.AnalyzeResponse analyze(String language, String versionOrNull, String content) {
        String version = (versionOrNull == null || versionOrNull.isBlank()) ? "1.0" : versionOrNull;
        return lang.analyze(new LanguageDtos.AnalyzeRequest(language, version, content));
    }

    public LanguageDtos.ExecuteResponse execute(String language, String versionOrNull, String content) {
        String version = (versionOrNull == null || versionOrNull.isBlank()) ? "1.0" : versionOrNull;
        return lang.execute(new LanguageDtos.ExecuteRequest(language, version, content));
    }

    public List<Snippet> list() {
        return repo.findAll();
    }
}
