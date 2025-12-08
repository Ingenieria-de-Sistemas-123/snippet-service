package com.snippetsearcher.snippet.repository.specification;

import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.model.SnippetComplianceStatus;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class SnippetSpecifications {

  private SnippetSpecifications() {}

  public static Specification<Snippet> ownedBy(UUID ownerId) {
    return (root, query, cb) -> cb.equal(root.get("ownerUserId"), ownerId);
  }

  public static Specification<Snippet> withIds(Collection<UUID> ids) {
    if (ids == null || ids.isEmpty()) {
      return (root, query, cb) -> cb.disjunction();
    }
    return (root, query, cb) -> root.get("id").in(ids);
  }

  public static Specification<Snippet> nameContains(String name) {
    if (!StringUtils.hasText(name)) {
      return null;
    }
    return (root, query, cb) ->
        cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%");
  }

  public static Specification<Snippet> languageEquals(String language) {
    if (!StringUtils.hasText(language)) {
      return null;
    }
    return (root, query, cb) ->
        cb.equal(cb.lower(root.get("language")), language.trim().toLowerCase());
  }

  public static Specification<Snippet> withComplianceStatus(SnippetComplianceStatus status) {
    if (status == null) {
      return null;
    }
    return (root, query, cb) -> cb.equal(root.get("complianceStatus"), status);
  }
}
