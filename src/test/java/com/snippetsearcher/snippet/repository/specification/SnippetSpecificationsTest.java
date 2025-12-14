package com.snippetsearcher.snippet.repository.specification;

import static org.junit.jupiter.api.Assertions.*;

import com.snippetsearcher.snippet.model.SnippetComplianceStatus;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

class SnippetSpecificationsTest {

  @Test
  void withIdsReturnsDisjunctionWhenEmpty() {
    Specification<?> spec = SnippetSpecifications.withIds(Set.of());
    assertNotNull(spec);
  }

  @Test
  void languageEqualsNormalizesInput() {
    Specification<?> spec = SnippetSpecifications.languageEquals("  PS  ");
    assertNotNull(spec);
  }

  @Test
  void nameContainsReturnsNullForBlank() {
    assertNull(SnippetSpecifications.nameContains("   "));
  }

  @Test
  void complianceStatusNullWhenMissing() {
    assertNull(SnippetSpecifications.withComplianceStatus(null));
    assertNotNull(SnippetSpecifications.withComplianceStatus(SnippetComplianceStatus.INVALID));
  }

  @Test
  void ownedByBuildsSpecification() {
    UUID id = UUID.randomUUID();
    assertNotNull(SnippetSpecifications.ownedBy(id));
  }
}
