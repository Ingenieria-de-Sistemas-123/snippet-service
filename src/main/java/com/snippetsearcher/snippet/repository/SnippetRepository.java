package com.snippetsearcher.snippet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.snippetsearcher.snippet.model.Snippet;
import java.util.UUID;

public interface SnippetRepository extends JpaRepository<Snippet, UUID> {}