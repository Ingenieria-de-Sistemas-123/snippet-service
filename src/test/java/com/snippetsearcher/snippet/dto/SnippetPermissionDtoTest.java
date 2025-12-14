package com.snippetsearcher.snippet.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class SnippetPermissionDtoTest {

  @Test
  void accessorsReturnConstructorValues() {
    UUID snippetId = UUID.randomUUID();

    SnippetPermissionDto dto = new SnippetPermissionDto(snippetId, PermissionTypeDto.OWNER);

    assertEquals(snippetId, dto.snippetId());
    assertEquals(PermissionTypeDto.OWNER, dto.type());
  }

  @Test
  void equalsAndHashCodeWorkCorrectly() {
    UUID snippetId = UUID.randomUUID();

    SnippetPermissionDto a = new SnippetPermissionDto(snippetId, PermissionTypeDto.SHARED);
    SnippetPermissionDto b = new SnippetPermissionDto(snippetId, PermissionTypeDto.SHARED);
    SnippetPermissionDto c = new SnippetPermissionDto(snippetId, PermissionTypeDto.OWNER);

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());

    assertNotEquals(a, c);
    assertNotEquals(a, null);
    assertNotEquals(a, new Object());
  }

  @Test
  void toStringContainsClassAndFields() {
    SnippetPermissionDto dto =
        new SnippetPermissionDto(
            UUID.fromString("00000000-0000-0000-0000-000000000001"), PermissionTypeDto.OWNER);

    String str = dto.toString();

    assertTrue(str.contains("SnippetPermissionDto"));
    assertTrue(str.contains("00000000-0000-0000-0000-000000000001"));
    assertTrue(str.contains("OWNER"));
  }

  @Test
  void allowsNullValues() {
    SnippetPermissionDto dto = new SnippetPermissionDto(null, null);

    assertNull(dto.snippetId());
    assertNull(dto.type());
  }
}
