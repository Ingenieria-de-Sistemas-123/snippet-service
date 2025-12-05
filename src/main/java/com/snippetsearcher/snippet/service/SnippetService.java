package com.snippetsearcher.snippet.service;

import com.snippetsearcher.snippet.dto.LanguageDtos;
import com.snippetsearcher.snippet.language.LanguageClient;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.repository.SnippetRepository;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class SnippetService {
  private final LanguageClient lang;
  private final SnippetRepository repo;

  public SnippetService(LanguageClient lang, SnippetRepository repo) {
    this.lang = lang;
    this.repo = repo;
  }

  public Snippet create(
          String name,
          String description,
          String language,
          String versionOrNull,
          String content
  ) {
    String version = (versionOrNull == null || versionOrNull.isBlank()) ? "1.0" : versionOrNull;

    var vRes = lang.validate(new LanguageDtos.ValidateRequest(language, version, content));
    if (vRes == null || !vRes.valid()) {
      throw new IllegalArgumentException(
              "Invalid source: " + (vRes == null ? "unknown" : vRes.errors()));
    }

    String finalContent = content;
    String finalVersion = version;

    var fRes = lang.format(new LanguageDtos.FormatRequest(language, finalVersion, content, false));
    if (fRes != null && fRes.formatted() != null && !fRes.formatted().isBlank()) {
      finalContent = fRes.formatted();
    }
    return repo.save(Snippet.of(name, description, language, finalVersion, finalContent));
  }

  public LanguageDtos.AnalyzeResponse analyze(
          String language, String versionOrNull, String content) {
    String version = (versionOrNull == null || versionOrNull.isBlank()) ? "1.0" : versionOrNull;
    return lang.analyze(new LanguageDtos.AnalyzeRequest(language, version, content));
  }

  public LanguageDtos.ExecuteResponse execute(
          String language, String versionOrNull, String content) {
    String version = (versionOrNull == null || versionOrNull.isBlank()) ? "1.0" : versionOrNull;
    return lang.execute(new LanguageDtos.ExecuteRequest(language, version, content));
  }

  public List<Snippet> list() {
    return repo.findAll();
  }

  public Snippet update(
          UUID id,
          String name,
          String description,
          String language,
          String versionOrNull,
          String content,
          String requesterSub // 👈 por ahora no lo usamos, lo dejamos para el futuro
  ) {
    // 1) buscar el snippet
    Snippet snippet = repo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Snippet not found"));

    // ⚠️ POR AHORA: no tenemos owner guardado en la tabla snippet,
    // así que no podemos verificar que requesterSub sea el dueño.
    // Más adelante esto se hará contra permission-service.

    // 2) normalizar versión
    String version = (versionOrNull == null || versionOrNull.isBlank()) ? "1.0" : versionOrNull;

    // 3) validar con language-service
    var vRes = lang.validate(new LanguageDtos.ValidateRequest(language, version, content));
    if (vRes == null || !vRes.valid()) {
      throw new IllegalArgumentException(
              "Invalid source: " + (vRes == null ? "unknown" : vRes.errors()));
    }

    // 4) formatear
    String finalContent = content;
    String finalVersion = version;

    var fRes = lang.format(new LanguageDtos.FormatRequest(language, finalVersion, content, false));
    if (fRes != null && fRes.formatted() != null && !fRes.formatted().isBlank()) {
      finalContent = fRes.formatted();
    }

    // 5) aplicar cambios al snippet existente
    snippet.update(name, description, language, finalVersion, finalContent);

    // 6) persistir y devolver
    return repo.save(snippet);
  }
}