package com.snippetsearcher.snippet.service;

import com.snippetsearcher.snippet.dto.LanguageDtos;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.repository.SnippetRepository;
import com.snippetsearcher.snippet.service.exception.SnippetNotFoundException;
import com.snippetsearcher.snippet.service.exception.SnippetValidationException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SnippetService {
  private final SnippetRepository repo;
  private final LanguageValidationService validator;

  public SnippetService(SnippetRepository repo, LanguageValidationService validator) {
    this.repo = repo;
    this.validator = validator;
  }

  @Transactional
  public Snippet create(String name, String description, String language, String version, String content) {
    LanguageDtos.ValidateResponse resp = validator.validate(language, version, content);
    if (resp == null || !resp.valid()) {
      List<LanguageDtos.ValidationError> errors = resp == null ? List.of() : resp.errors();
      throw new SnippetValidationException("Snippet inválido", errors);
    }
    return repo.save(Snippet.of(name, description, language, version, content));
  }

  @Transactional
  public Snippet update(UUID id, String name, String description, String language, String version, String content) {
    Snippet s = repo.findById(id).orElseThrow(() -> new SnippetNotFoundException(id));
    LanguageDtos.ValidateResponse resp = validator.validate(language, version, content);
    if (resp == null || !resp.valid()) {
      List<LanguageDtos.ValidationError> errors = resp == null ? List.of() : resp.errors();
      throw new SnippetValidationException("Snippet inválido", errors);
    }
    s.update(name, description, language, version, content);
    return repo.save(s);
  }

  @Transactional(readOnly = true)
  public List<Snippet> list() {
    return repo.findAll();
  }
}

