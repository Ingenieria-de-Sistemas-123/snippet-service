package com.snippetsearcher.snippet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SnippetRepository extends JpaRepository<Snippet, UUID> {}