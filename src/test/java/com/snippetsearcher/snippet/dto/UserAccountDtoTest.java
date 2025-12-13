package com.snippetsearcher.snippet.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserAccountDtoTest {

  @Test
  void accessorsReturnConstructorValues() {
    UUID id = UUID.randomUUID();

    UserAccountDto dto =
        new UserAccountDto(
            id, "auth0|123", "user@test.com", "User Name", "https://img.test/pic.png");

    assertEquals(id, dto.id());
    assertEquals("auth0|123", dto.auth0Sub());
    assertEquals("user@test.com", dto.email());
    assertEquals("User Name", dto.name());
    assertEquals("https://img.test/pic.png", dto.picture());
  }

  @Test
  void equalsAndHashCodeWorkCorrectly() {
    UUID id = UUID.randomUUID();

    UserAccountDto a = new UserAccountDto(id, "auth0|1", "a@test.com", "A", "pic");
    UserAccountDto b = new UserAccountDto(id, "auth0|1", "a@test.com", "A", "pic");
    UserAccountDto c = new UserAccountDto(id, "auth0|2", "a@test.com", "A", "pic");

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertNotEquals(a, c);

    assertNotEquals(a, null);
    assertNotEquals(a, new Object());
  }

  @Test
  void toStringContainsClassAndFields() {
    UserAccountDto dto =
        new UserAccountDto(
            UUID.fromString("00000000-0000-0000-0000-000000000001"),
            "auth0|abc",
            "x@test.com",
            "X",
            null);

    String str = dto.toString();

    assertTrue(str.contains("UserAccountDto"));
    assertTrue(str.contains("00000000-0000-0000-0000-000000000001"));
    assertTrue(str.contains("auth0|abc"));
    assertTrue(str.contains("x@test.com"));
    assertTrue(str.contains("X"));
  }

  @Test
  void allowsNullValues() {
    UserAccountDto dto = new UserAccountDto(null, null, null, null, null);

    assertNull(dto.id());
    assertNull(dto.auth0Sub());
    assertNull(dto.email());
    assertNull(dto.name());
    assertNull(dto.picture());
  }
}
