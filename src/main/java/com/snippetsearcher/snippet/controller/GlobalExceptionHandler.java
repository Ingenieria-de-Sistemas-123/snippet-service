package com.snippetsearcher.snippet.controller;

import com.snippetsearcher.snippet.service.exception.SnippetNotFoundException;
import com.snippetsearcher.snippet.service.exception.SnippetValidationException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(SnippetValidationException.class)
  public ResponseEntity<?> handleValidation(SnippetValidationException ex) {
    return ResponseEntity.badRequest().body(Map.of("valid", false, "errors", ex.getErrors()));
  }

  @ExceptionHandler(SnippetNotFoundException.class)
  public ResponseEntity<?> handleNotFound(SnippetNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleGeneric(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal error"));
  }
}

