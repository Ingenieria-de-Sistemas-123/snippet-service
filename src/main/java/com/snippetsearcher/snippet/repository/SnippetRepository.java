package com.snippetsearcher.snippet.repository;

import com.snippetsearcher.snippet.model.Snippet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SnippetRepository
    extends JpaRepository<Snippet, UUID>, JpaSpecificationExecutor<Snippet> {

  Optional<Snippet> findByIdAndOwnerUserId(UUID id, UUID ownerUserId);

  List<Snippet> findAllByOwnerUserId(UUID ownerUserId);
}
