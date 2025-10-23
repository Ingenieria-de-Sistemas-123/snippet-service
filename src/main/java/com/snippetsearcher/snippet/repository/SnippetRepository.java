package com.snippetsearcher.snippet.repository;

import com.snippetsearcher.snippet.model.Snippet;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SnippetRepository extends JpaRepository<Snippet, UUID> {}
