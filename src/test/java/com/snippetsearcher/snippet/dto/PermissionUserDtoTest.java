package com.snippetsearcher.snippet.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class PermissionUserDtoTest {

  @Test
  void accessorsReturnConstructorValues() {
    UUID id = UUID.randomUUID();

    PermissionUserDto dto = new PermissionUserDto(id, "Juan Perez", "juan.perez@test.com");

    assertEquals(id, dto.id());
    assertEquals("Juan Perez", dto.name());
    assertEquals("juan.perez@test.com", dto.email());
  }

  @Test
  void equalsAndHashCodeWorkCorrectly() {
    UUID id = UUID.randomUUID();

    PermissionUserDto a = new PermissionUserDto(id, "Ana", "ana@test.com");
    PermissionUserDto b = new PermissionUserDto(id, "Ana", "ana@test.com");
    PermissionUserDto c = new PermissionUserDto(id, "Ana", "otro@test.com");

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());

    assertNotEquals(a, c);
    assertNotEquals(a, null);
    assertNotEquals(a, new Object());
  }

  @Test
  void toStringContainsClassAndFields() {
    PermissionUserDto dto =
        new PermissionUserDto(
            UUID.fromString("00000000-0000-0000-0000-000000000001"), "Test User", "test@test.com");

    String str = dto.toString();

    assertTrue(str.contains("PermissionUserDto"));
    assertTrue(str.contains("Test User"));
    assertTrue(str.contains("test@test.com"));
  }

  @Test
  void allowsNullValues() {
    PermissionUserDto dto = new PermissionUserDto(null, null, null);

    assertNull(dto.id());
    assertNull(dto.name());
    assertNull(dto.email());
  }
}
