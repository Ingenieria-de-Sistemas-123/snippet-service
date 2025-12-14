package com.snippetsearcher.snippet.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class SnippetRequestsRecordTest {

  @Test
  void createSnippetRequest_accessorsAndEquality() {
    CreateSnippetRequest a = new CreateSnippetRequest("N", "D", "ps", "1.0");
    CreateSnippetRequest b = new CreateSnippetRequest("N", "D", "ps", "1.0");
    CreateSnippetRequest c = new CreateSnippetRequest("N2", "D", "ps", "1.0");

    assertEquals("N", a.name());
    assertEquals("D", a.description());
    assertEquals("ps", a.language());
    assertEquals("1.0", a.version());

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertNotEquals(a, c);
    assertTrue(a.toString().contains("CreateSnippetRequest"));
  }

  @Test
  void updateSnippetRequest_accessorsAndEquality() {
    UpdateSnippetRequest a = new UpdateSnippetRequest("N", "D", "ps", "1.0");
    UpdateSnippetRequest b = new UpdateSnippetRequest("N", "D", "ps", "1.0");

    assertEquals(a, b);
    assertEquals("ps", a.language());
    assertTrue(a.toString().contains("UpdateSnippetRequest"));
  }

  @Test
  void formatSnippetRequest_accessorsAndNullsAllowed() {
    FormatSnippetRequest req = new FormatSnippetRequest("x", null, null, null);
    assertEquals("x", req.content());
    assertNull(req.language());
    assertNull(req.version());
    assertNull(req.check());
  }

  @Test
  void createSnippetTestRequest_accessorsAndEquality() {
    CreateSnippetTestRequest a = new CreateSnippetTestRequest("T", "S");
    CreateSnippetTestRequest b = new CreateSnippetTestRequest("T", "S");

    assertEquals(a, b);
    assertEquals("T", a.name());
    assertEquals("S", a.script());
  }

  @Test
  void updateSnippetTestRequest_accessorsAndEquality() {
    UpdateSnippetTestRequest a = new UpdateSnippetTestRequest("T", "S");
    UpdateSnippetTestRequest b = new UpdateSnippetTestRequest("T", "S");

    assertEquals(a, b);
    assertTrue(a.toString().contains("UpdateSnippetTestRequest"));
  }

  @Test
  void ruleRequest_accessorsAndEquality() {
    RuleRequest a = new RuleRequest("id", "name", true, 2);
    RuleRequest b = new RuleRequest("id", "name", true, 2);
    RuleRequest c = new RuleRequest("id", "name", false, 2);

    assertEquals("id", a.id());
    assertEquals("name", a.name());
    assertTrue(a.isActive());
    assertEquals(2, a.value());

    assertEquals(a, b);
    assertNotEquals(a, c);
  }

  @Test
  void shareSnippetRequest_accessorsAndEquality() {
    UUID id = UUID.randomUUID();
    ShareSnippetRequest a = new ShareSnippetRequest(id);
    ShareSnippetRequest b = new ShareSnippetRequest(id);

    assertEquals(a, b);
    assertEquals(id, a.userId());
    assertTrue(a.toString().contains("ShareSnippetRequest"));
  }
}
