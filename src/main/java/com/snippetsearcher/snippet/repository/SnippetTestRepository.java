package com.snippetsearcher.snippet.repository;

import com.snippetsearcher.snippet.model.SnippetTest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SnippetTestRepository extends JpaRepository<SnippetTest, UUID> {

  List<SnippetTest> findBySnippetId(UUID snippetId);

  Optional<SnippetTest> findByIdAndSnippetId(UUID id, UUID snippetId);
}
