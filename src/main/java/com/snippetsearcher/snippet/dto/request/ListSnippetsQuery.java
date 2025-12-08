package com.snippetsearcher.snippet.dto.request;

import com.snippetsearcher.snippet.model.SnippetComplianceStatus;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

public record ListSnippetsQuery(
    int page,
    int pageSize,
    String name,
    String language,
    Boolean valid,
    Relation relation,
    SortField sortField,
    Sort.Direction sortDirection) {

  public static ListSnippetsQuery from(
      int page,
      int pageSize,
      String name,
      String language,
      Boolean valid,
      String relationValue,
      String sortBy,
      String sortDirection) {
    Relation parsedRelation = Relation.from(relationValue);
    SortField parsedSortField = SortField.from(sortBy);
    Sort.Direction direction = parseDirection(sortDirection);

    return new ListSnippetsQuery(
        page,
        pageSize,
        normalize(name),
        normalize(language),
        valid,
        parsedRelation,
        parsedSortField,
        direction);
  }

  public Sort sort() {
    Sort sort = Sort.by(sortDirection, sortField.property);
    if (sortField != SortField.UPDATED_AT) {
      sort = sort.and(Sort.by(Sort.Direction.DESC, SortField.UPDATED_AT.property));
    }
    return sort;
  }

  public SnippetComplianceStatus complianceFilter() {
    if (valid == null) {
      return null;
    }
    return Boolean.TRUE.equals(valid)
        ? SnippetComplianceStatus.VALID
        : SnippetComplianceStatus.INVALID;
  }

  private static Sort.Direction parseDirection(String value) {
    if (!StringUtils.hasText(value)) {
      return Sort.Direction.DESC;
    }
    try {
      return Sort.Direction.valueOf(value.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Parámetro sort_dir inválido. Valores permitidos: asc, desc.");
    }
  }

  private static String normalize(String value) {
    return StringUtils.hasText(value) ? value.trim() : null;
  }

  public enum Relation {
    OWNED,
    SHARED,
    ALL;

    public boolean includesOwned() {
      return this == OWNED || this == ALL;
    }

    public boolean includesShared() {
      return this == SHARED || this == ALL;
    }

    public static Relation from(String value) {
      if (!StringUtils.hasText(value)) {
        return ALL;
      }
      String normalized = value.trim().toUpperCase();
      if ("OWNER".equals(normalized)) {
        normalized = "OWNED";
      }
      try {
        return Relation.valueOf(normalized);
      } catch (IllegalArgumentException ex) {
        throw new IllegalArgumentException(
            "Parámetro relation inválido. Valores permitidos: owned, shared, all.");
      }
    }
  }

  public enum SortField {
    UPDATED_AT("updatedAt"),
    NAME("name"),
    LANGUAGE("language"),
    COMPLIANCE("complianceStatus");

    private final String property;

    SortField(String property) {
      this.property = property;
    }

    public static SortField from(String value) {
      if (!StringUtils.hasText(value)) {
        return UPDATED_AT;
      }
      try {
        String normalized =
            value.trim().replace('-', '_').replace(' ', '_').toUpperCase();
        if ("UPDATEDAT".equals(normalized)) {
          normalized = "UPDATED_AT";
        }
        return SortField.valueOf(normalized);
      } catch (IllegalArgumentException ex) {
        throw new IllegalArgumentException(
            "Parámetro sort_by inválido. Valores permitidos: updated_at, name, language, compliance.");
      }
    }
  }
}
