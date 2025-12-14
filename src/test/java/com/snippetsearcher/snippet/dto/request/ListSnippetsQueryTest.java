package com.snippetsearcher.snippet.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.snippetsearcher.snippet.model.SnippetComplianceStatus;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

class ListSnippetsQueryTest {

  @Test
  void parsesAndNormalizesInputValues() {
    ListSnippetsQuery query =
        ListSnippetsQuery.from(0, 20, " name ", " lang ", true, "shared", "language", "asc");

    assertEquals("name", query.name());
    assertEquals("lang", query.language());
    assertEquals(ListSnippetsQuery.Relation.SHARED, query.relation());
    assertEquals(SnippetComplianceStatus.VALID, query.complianceFilter());
    Sort sort = query.sort();
    assertEquals("language", sort.getOrderFor("language").getProperty());
    assertEquals(Sort.Direction.ASC, sort.getOrderFor("language").getDirection());
  }

  @Test
  void relationAliasOwnerIsSupported() {
    ListSnippetsQuery query =
        ListSnippetsQuery.from(1, 5, null, null, null, "owner", "updated_at", "desc");

    assertEquals(ListSnippetsQuery.Relation.OWNED, query.relation());
    assertTrue(query.relation().includesOwned());
    assertFalse(query.relation().includesShared());
    assertEquals(Sort.Direction.DESC, query.sort().getOrderFor("updatedAt").getDirection());
  }

  @Test
  void throwsOnInvalidSortDirection() {
    assertThrows(
        IllegalArgumentException.class,
        () -> ListSnippetsQuery.from(0, 10, null, null, null, "all", "name", "wrong-direction"));
  }

  @Test
  void from_defaults() {
    ListSnippetsQuery q = ListSnippetsQuery.from(0, 10, null, null, null, null, null, null);
    assertEquals(ListSnippetsQuery.Relation.ALL, q.relation());
    assertEquals(ListSnippetsQuery.SortField.UPDATED_AT, q.sortField());
    assertEquals(Sort.Direction.DESC, q.sortDirection());
    assertNull(q.complianceFilter());
  }

  @Test
  void from_normalizes_trims() {
    ListSnippetsQuery q =
        ListSnippetsQuery.from(0, 10, "  hi  ", "  ps ", true, "owner", " name ", "asc");
    assertEquals("hi", q.name());
    assertEquals("ps", q.language());
    assertEquals(ListSnippetsQuery.Relation.OWNED, q.relation());
    assertEquals(ListSnippetsQuery.SortField.NAME, q.sortField());
    assertEquals(Sort.Direction.ASC, q.sortDirection());
    assertEquals(SnippetComplianceStatus.VALID, q.complianceFilter());
  }

  @Test
  void relation_parses_owner_alias() {
    assertEquals(ListSnippetsQuery.Relation.OWNED, ListSnippetsQuery.Relation.from("OWNER"));
    assertEquals(ListSnippetsQuery.Relation.OWNED, ListSnippetsQuery.Relation.from("owned"));
    assertEquals(ListSnippetsQuery.Relation.SHARED, ListSnippetsQuery.Relation.from("shared"));
    assertEquals(ListSnippetsQuery.Relation.ALL, ListSnippetsQuery.Relation.from(null));
  }

  @Test
  void relation_invalid_throws() {
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> ListSnippetsQuery.Relation.from("xxx"));
    assertTrue(ex.getMessage().contains("relation"));
  }

  @Test
  void sortField_accepts_updatedat_aliases() {
    assertEquals(
        ListSnippetsQuery.SortField.UPDATED_AT, ListSnippetsQuery.SortField.from("updatedat"));
    assertEquals(
        ListSnippetsQuery.SortField.UPDATED_AT, ListSnippetsQuery.SortField.from("updated_at"));
    assertEquals(
        ListSnippetsQuery.SortField.COMPLIANCE, ListSnippetsQuery.SortField.from("compliance"));
  }

  @Test
  void sortField_invalid_throws() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class, () -> ListSnippetsQuery.SortField.from("nope"));
    assertTrue(ex.getMessage().contains("sort_by"));
  }

  @Test
  void sort_adds_secondary_updatedAt_when_primary_not_updatedAt() {
    ListSnippetsQuery q = ListSnippetsQuery.from(0, 10, null, null, null, "all", "name", "asc");

    Sort s = q.sort();
    String str = s.toString().toLowerCase();
    assertTrue(str.contains("name"));
    assertTrue(str.contains("updatedat"));
  }

  @Test
  void sortDirection_invalid_throws() {
    IllegalArgumentException ex =
        assertThrows(
            IllegalArgumentException.class,
            () -> ListSnippetsQuery.from(0, 10, null, null, null, "all", "name", "wat"));
    assertTrue(ex.getMessage().contains("sort_dir"));
  }

  @Test
  void from_defaults_work() {
    var q = ListSnippetsQuery.from(0, 10, null, null, null, null, null, null);

    assertEquals(0, q.page());
    assertEquals(10, q.pageSize());
    assertEquals(ListSnippetsQuery.Relation.ALL, q.relation());
    assertEquals(ListSnippetsQuery.SortField.UPDATED_AT, q.sortField());
    assertEquals(Sort.Direction.DESC, q.sortDirection());
  }

  @Test
  void complianceFilter_valid_true() {
    var q = ListSnippetsQuery.from(0, 10, null, null, true, "all", "updated_at", "asc");

    assertEquals(SnippetComplianceStatus.VALID, q.complianceFilter());
  }

  @Test
  void complianceFilter_valid_false() {
    var q = ListSnippetsQuery.from(0, 10, null, null, false, "all", "updated_at", "asc");

    assertEquals(SnippetComplianceStatus.INVALID, q.complianceFilter());
  }

  @Test
  void complianceFilter_null_when_not_provided() {
    var q = ListSnippetsQuery.from(0, 10, null, null, null, "all", "updated_at", "asc");

    assertNull(q.complianceFilter());
  }

  @Test
  void relation_parses_owner_as_owned() {
    var q = ListSnippetsQuery.from(0, 10, null, null, null, "owner", "updated_at", "asc");

    assertEquals(ListSnippetsQuery.Relation.OWNED, q.relation());
  }

  @Test
  void sortField_parses_dash_and_case() {
    var q = ListSnippetsQuery.from(0, 10, null, null, null, "all", "updated-at", "desc");

    assertEquals(ListSnippetsQuery.SortField.UPDATED_AT, q.sortField());
  }

  @Test
  void sort_contains_secondary_updated_at() {
    var q = ListSnippetsQuery.from(0, 10, null, null, null, "all", "name", "asc");

    var sort = q.sort();

    assertEquals("name", sort.iterator().next().getProperty());
    assertTrue(sort.stream().anyMatch(o -> o.getProperty().equals("updatedAt")));
  }
}
