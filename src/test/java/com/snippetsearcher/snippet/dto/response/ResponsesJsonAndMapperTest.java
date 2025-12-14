package com.snippetsearcher.snippet.dto.response;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippetsearcher.snippet.dto.PermissionTypeDto;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.model.SnippetComplianceStatus;
import com.snippetsearcher.snippet.model.SnippetTest;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ResponsesJsonAndMapperTest {

  private final ObjectMapper mapper = new ObjectMapper();

  @Test
  void listSnippetsResponse_serializesJsonPropertyNames() throws Exception {
    ListSnippetsResponse r = new ListSnippetsResponse(1, 20, 123L, List.of());

    String json = mapper.writeValueAsString(r);
    JsonNode node = mapper.readTree(json);

    assertTrue(node.has("page"));
    assertTrue(node.has("page_size"));
    assertTrue(node.has("count"));
    assertTrue(node.has("snippets"));

    assertEquals(20, node.get("page_size").asInt());
    assertEquals(123L, node.get("count").asLong());
  }

  @Test
  void snippetListItemResponse_jsonIncludeNonNull_doesNotSerializeNullAuthor() throws Exception {
    SnippetListItemResponse r =
        new SnippetListItemResponse(
            UUID.randomUUID(),
            "n",
            "ps",
            ".ps",
            null, // author null
            SnippetComplianceStatus.VALID,
            true,
            PermissionTypeDto.OWNER);

    String json = mapper.writeValueAsString(r);
    JsonNode node = mapper.readTree(json);

    assertFalse(node.has("author")); // @JsonInclude(NON_NULL)
  }

  @Test
  void snippetListItemResponse_fromEntity_mapsFields_andSetsValidFlag() {
    UUID id = UUID.randomUUID();
    UUID owner = UUID.randomUUID();

    Snippet s = mock(Snippet.class);
    when(s.getId()).thenReturn(id);
    when(s.getName()).thenReturn("My");
    when(s.getLanguage()).thenReturn("ps");
    when(s.getOwnerUserId()).thenReturn(owner);
    when(s.getComplianceStatus()).thenReturn(SnippetComplianceStatus.VALID);

    SnippetListItemResponse r =
        SnippetListItemResponse.fromEntity(s, ".ps", PermissionTypeDto.SHARED);

    assertEquals(id, r.id());
    assertEquals("My", r.name());
    assertEquals("ps", r.language());
    assertEquals(".ps", r.extension());
    assertEquals(owner.toString(), r.author());
    assertEquals(SnippetComplianceStatus.VALID, r.complianceStatus());
    assertTrue(r.valid());
    assertEquals(PermissionTypeDto.SHARED, r.relation());
  }

  @Test
  void snippetListItemResponse_fromEntity_whenOwnerNull_authorIsNull() {
    Snippet s = mock(Snippet.class);
    when(s.getId()).thenReturn(UUID.randomUUID());
    when(s.getName()).thenReturn("My");
    when(s.getLanguage()).thenReturn("ps");
    when(s.getOwnerUserId()).thenReturn(null);
    when(s.getComplianceStatus()).thenReturn(SnippetComplianceStatus.INVALID);

    SnippetListItemResponse r =
        SnippetListItemResponse.fromEntity(s, ".ps", PermissionTypeDto.OWNER);

    assertNull(r.author());
    assertFalse(r.valid()); // INVALID => valid=false
  }

  @Test
  void snippetResponse_fromEntity_fullOverload_mapsFields() {
    UUID id = UUID.randomUUID();
    UUID owner = UUID.randomUUID();
    OffsetDateTime created = OffsetDateTime.parse("2025-01-01T00:00:00Z");
    OffsetDateTime updated = OffsetDateTime.parse("2025-01-02T00:00:00Z");

    Snippet s = mock(Snippet.class);
    when(s.getId()).thenReturn(id);
    when(s.getName()).thenReturn("N");
    when(s.getLanguage()).thenReturn("ps");
    when(s.getVersion()).thenReturn("1.0");
    when(s.getDescription()).thenReturn("D");
    when(s.getAssetKey()).thenReturn("snippets/a.ps");
    when(s.getOwnerUserId()).thenReturn(owner);
    when(s.getCreatedAt()).thenReturn(created);
    when(s.getUpdatedAt()).thenReturn(updated);

    List<SnippetLintErrorResponse> lint = List.of(new SnippetLintErrorResponse("r", 1, 2, "m"));
    List<SnippetTestResponse> tests =
        List.of(new SnippetTestResponse(UUID.randomUUID(), "t", "d", null, null, null, null));

    SnippetResponse r =
        SnippetResponse.fromEntity(s, "content", lint, tests, SnippetComplianceStatus.VALID, "ok");

    assertEquals(id, r.id());
    assertEquals("N", r.name());
    assertEquals("ps", r.language());
    assertEquals("1.0", r.version());
    assertEquals("D", r.description());
    assertEquals("snippets/a.ps", r.assetKey());
    assertEquals(owner, r.ownerUserId());
    assertEquals(SnippetComplianceStatus.VALID, r.complianceStatus());
    assertEquals("ok", r.complianceMessage());
    assertEquals("content", r.content());
    assertEquals(lint, r.lintErrors());
    assertEquals(tests, r.tests());
    assertEquals(created, r.createdAt());
    assertEquals(updated, r.updatedAt());
  }

  @Test
  void snippetResponse_fromEntity_defaultOverload_usesEntityComplianceAndDefaults() {
    UUID id = UUID.randomUUID();

    Snippet s = mock(Snippet.class);
    when(s.getId()).thenReturn(id);
    when(s.getName()).thenReturn("N");
    when(s.getLanguage()).thenReturn("ps");
    when(s.getVersion()).thenReturn("1.0");
    when(s.getDescription()).thenReturn(null);
    when(s.getAssetKey()).thenReturn("k");
    when(s.getOwnerUserId()).thenReturn(UUID.randomUUID());
    when(s.getComplianceStatus()).thenReturn(SnippetComplianceStatus.INVALID);
    when(s.getComplianceMessage()).thenReturn("bad");
    when(s.getCreatedAt()).thenReturn(OffsetDateTime.parse("2025-01-01T00:00:00Z"));
    when(s.getUpdatedAt()).thenReturn(OffsetDateTime.parse("2025-01-02T00:00:00Z"));

    SnippetResponse r = SnippetResponse.fromEntity(s);

    assertEquals(id, r.id());
    assertEquals(SnippetComplianceStatus.INVALID, r.complianceStatus());
    assertEquals("bad", r.complianceMessage());
    assertNull(r.content());
    assertNotNull(r.lintErrors());
    assertNotNull(r.tests());
    assertTrue(r.lintErrors().isEmpty());
    assertTrue(r.tests().isEmpty());
  }

  @Test
  void snippetTestResponse_fromEntity_mapsAllFields() {
    UUID id = UUID.randomUUID();
    OffsetDateTime last = OffsetDateTime.parse("2025-06-01T00:00:00Z");

    SnippetTest t = mock(SnippetTest.class);
    when(t.getId()).thenReturn(id);
    when(t.getName()).thenReturn("T");
    when(t.getDescription()).thenReturn("D");
    when(t.getLastRunAt()).thenReturn(last);
    when(t.getLastRunExitCode()).thenReturn(0);
    when(t.getLastRunOutput()).thenReturn("out");
    when(t.getLastRunError()).thenReturn("");

    SnippetTestResponse r = SnippetTestResponse.fromEntity(t);

    assertEquals(id, r.id());
    assertEquals("T", r.name());
    assertEquals("D", r.description());
    assertEquals(last, r.lastRunAt());
    assertEquals(0, r.lastRunExitCode());
    assertEquals("out", r.lastRunOutput());
    assertEquals("", r.lastRunError());
  }
}
