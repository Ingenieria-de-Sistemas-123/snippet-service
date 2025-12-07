package com.snippetsearcher.snippet.repository;

import com.snippetsearcher.snippet.model.Snippet;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SnippetRepository extends JpaRepository<Snippet, UUID> {

  Page<Snippet> findByOwnerUserId(UUID ownerUserId, Pageable pageable);

  Page<Snippet> findByOwnerUserIdAndNameContainingIgnoreCase(
      UUID ownerUserId, String name, Pageable pageable);

  Optional<Snippet> findByIdAndOwnerUserId(UUID id, UUID ownerUserId);
}
