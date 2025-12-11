package com.snippetsearcher.snippet.exception;

import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SnippetNotFoundException extends RuntimeException {

  public SnippetNotFoundException(UUID snippetId) {
    super("Snippet %s no encontrado o sin permisos para acceder.".formatted(snippetId));
  }
}
