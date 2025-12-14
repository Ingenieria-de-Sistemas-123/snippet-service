package com.snippetsearcher.snippet.dto.response;

import static org.junit.jupiter.api.Assertions.*;

import com.snippetsearcher.snippet.dto.PermissionTypeDto;
import com.snippetsearcher.snippet.model.SnippetComplianceStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ResponsesRecordTest {

  @Test
  void apiErrorResponse_recordWorks() {
    ApiErrorResponse a = new ApiErrorResponse("boom");
    ApiErrorResponse b = new ApiErrorResponse("boom");

    assertEquals("boom", a.message());
    assertEquals(a, b);
    assertTrue(a.toString().contains("ApiErrorResponse"));
  }

  @Test
  void fileTypeResponse_recordWorks() {
    FileTypeResponse a = new FileTypeResponse("ps", ".ps");
    FileTypeResponse b = new FileTypeResponse("ps", ".ps");

    assertEquals("ps", a.language());
    assertEquals(".ps", a.extension());
    assertEquals(a, b);
  }

  @Test
  void formatSnippetResponse_recordWorks() {
    FormatSnippetResponse r = new FormatSnippetResponse(true, "fmt", "orig", "diag");

    assertTrue(r.changed());
    assertEquals("fmt", r.formatted());
    assertEquals("orig", r.content());
    assertEquals("diag", r.diagnostics());
  }

  @Test
  void friendsResponse_recordWorks() {
    FriendsResponse a = new FriendsResponse("1", "Ana", "a@a.com");
    FriendsResponse b = new FriendsResponse("1", "Ana", "a@a.com");
    FriendsResponse c = new FriendsResponse("2", "Ana", "a@a.com");

    assertEquals(a, b);
    assertNotEquals(a, c);
    assertTrue(a.toString().contains("FriendsResponse"));
  }

  @Test
  void listSnippetsResponse_recordWorks() {
    SnippetListItemResponse item =
        new SnippetListItemResponse(
            UUID.randomUUID(),
            "N",
            "ps",
            ".ps",
            "author",
            SnippetComplianceStatus.VALID,
            true,
            PermissionTypeDto.OWNER);

    ListSnippetsResponse r = new ListSnippetsResponse(0, 10, 1L, List.of(item));

    assertEquals(0, r.page());
    assertEquals(10, r.pageSize());
    assertEquals(1L, r.count());
    assertEquals(1, r.snippets().size());
  }

  @Test
  void ruleResponse_recordWorks() {
    RuleResponse a = new RuleResponse("id", "name", true, 2);
    RuleResponse b = new RuleResponse("id", "name", true, 2);

    assertEquals(a, b);
    assertEquals("id", a.id());
    assertTrue(a.isActive());
    assertEquals(2, a.value());
  }

  @Test
  void snippetLintErrorResponse_recordWorks_andAllowsNulls() {
    SnippetLintErrorResponse e = new SnippetLintErrorResponse("r", null, null, "m");
    assertEquals("r", e.rule());
    assertNull(e.line());
    assertNull(e.column());
    assertEquals("m", e.message());
  }

  @Test
  void snippetTestExecutionResponse_recordWorks() {
    UUID id = UUID.randomUUID();
    OffsetDateTime now = OffsetDateTime.now();

    SnippetTestExecutionResponse r = new SnippetTestExecutionResponse(id, true, 0, "out", "", now);

    assertEquals(id, r.testId());
    assertTrue(r.passed());
    assertEquals(0, r.exitCode());
    assertEquals("out", r.output());
    assertEquals("", r.error());
    assertEquals(now, r.executedAt());
  }
}
