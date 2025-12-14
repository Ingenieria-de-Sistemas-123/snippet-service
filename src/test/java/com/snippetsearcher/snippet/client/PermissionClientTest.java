package com.snippetsearcher.snippet.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.snippetsearcher.snippet.dto.*;
import com.snippetsearcher.snippet.dto.request.CreatePermissionRequestDto;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

class PermissionClientTest {

  @Test
  void ensureUser_success_returnsBody_andSendsBearer() {
    RestTemplate rt = mock(RestTemplate.class);
    PermissionClient client = new PermissionClient(rt, "http://permission");

    UserAccountDto dto = mock(UserAccountDto.class);

    ArgumentCaptor<HttpEntity<Void>> captor = ArgumentCaptor.forClass(HttpEntity.class);

    when(rt.exchange(
            eq("http://permission/api/me/sync"),
            eq(HttpMethod.POST),
            captor.capture(),
            eq(UserAccountDto.class)))
        .thenReturn(new ResponseEntity<>(dto, HttpStatus.OK));

    UserAccountDto res = client.ensureUser("token123");

    assertSame(dto, res);

    HttpHeaders headers = captor.getValue().getHeaders();
    assertEquals("Bearer token123", headers.getFirst(HttpHeaders.AUTHORIZATION));
  }

  @Test
  void ensureUser_whenNon2xxOrNullBody_throws() {
    RestTemplate rt = mock(RestTemplate.class);
    PermissionClient client = new PermissionClient(rt, "http://permission");

    when(rt.exchange(
            eq("http://permission/api/me/sync"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(UserAccountDto.class)))
        .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

    assertThrows(IllegalStateException.class, () -> client.ensureUser("t"));

    when(rt.exchange(
            eq("http://permission/api/me/sync"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(UserAccountDto.class)))
        .thenReturn(new ResponseEntity<>(null, HttpStatus.BAD_REQUEST));

    assertThrows(IllegalStateException.class, () -> client.ensureUser("t"));
  }

  @Test
  void listSnippetPermissions_success_returnsList() {
    RestTemplate rt = mock(RestTemplate.class);
    PermissionClient client = new PermissionClient(rt, "http://permission");

    SnippetPermissionDto a = mock(SnippetPermissionDto.class);
    SnippetPermissionDto b = mock(SnippetPermissionDto.class);

    when(rt.exchange(
            eq("http://permission/api/me/snippets"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(SnippetPermissionDto[].class)))
        .thenReturn(new ResponseEntity<>(new SnippetPermissionDto[] {a, b}, HttpStatus.OK));

    List<SnippetPermissionDto> res = client.listSnippetPermissions("tok");

    assertEquals(2, res.size());
    assertSame(a, res.get(0));
    assertSame(b, res.get(1));
  }

  @Test
  void listSnippetPermissions_whenNon2xxOrNullBody_throws() {
    RestTemplate rt = mock(RestTemplate.class);
    PermissionClient client = new PermissionClient(rt, "http://permission");

    when(rt.exchange(
            eq("http://permission/api/me/snippets"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(SnippetPermissionDto[].class)))
        .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

    assertThrows(IllegalStateException.class, () -> client.listSnippetPermissions("tok"));

    when(rt.exchange(
            eq("http://permission/api/me/snippets"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(SnippetPermissionDto[].class)))
        .thenReturn(new ResponseEntity<>(null, HttpStatus.FORBIDDEN));

    assertThrows(IllegalStateException.class, () -> client.listSnippetPermissions("tok"));
  }

  @Test
  void createOwnerPermission_postsPermissionWithCorrectBodyAndHeaders() {
    RestTemplate rt = mock(RestTemplate.class);
    PermissionClient client = new PermissionClient(rt, "http://permission");

    UUID userId = UUID.randomUUID();
    UUID snippetId = UUID.randomUUID();

    ArgumentCaptor<HttpEntity<CreatePermissionRequestDto>> captor =
        ArgumentCaptor.forClass(HttpEntity.class);

    when(rt.exchange(
            eq("http://permission/api/permissions"),
            eq(HttpMethod.POST),
            captor.capture(),
            eq(Void.class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

    client.createOwnerPermission("tok", userId, snippetId);

    HttpEntity<CreatePermissionRequestDto> entity = captor.getValue();

    assertNotNull(entity.getBody());
    assertEquals(snippetId, entity.getBody().snippetId());
    assertEquals(userId, entity.getBody().userId());
    assertEquals(PermissionTypeDto.OWNER, entity.getBody().type());

    HttpHeaders headers = entity.getHeaders();
    assertEquals("Bearer tok", headers.getFirst(HttpHeaders.AUTHORIZATION));
    assertEquals(MediaType.APPLICATION_JSON, headers.getContentType());
  }

  @Test
  void createSharedPermission_postsPermissionWithSharedType() {
    RestTemplate rt = mock(RestTemplate.class);
    PermissionClient client = new PermissionClient(rt, "http://permission");

    UUID userId = UUID.randomUUID();
    UUID snippetId = UUID.randomUUID();

    ArgumentCaptor<HttpEntity<CreatePermissionRequestDto>> captor =
        ArgumentCaptor.forClass(HttpEntity.class);

    when(rt.exchange(
            eq("http://permission/api/permissions"),
            eq(HttpMethod.POST),
            captor.capture(),
            eq(Void.class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));

    client.createSharedPermission("tok", snippetId, userId);

    assertEquals(PermissionTypeDto.SHARED, captor.getValue().getBody().type());
  }

  @Test
  void createPermission_whenNon2xx_throws() {
    RestTemplate rt = mock(RestTemplate.class);
    PermissionClient client = new PermissionClient(rt, "http://permission");

    when(rt.exchange(
            eq("http://permission/api/permissions"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(Void.class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

    assertThrows(
        IllegalStateException.class,
        () -> client.createOwnerPermission("tok", UUID.randomUUID(), UUID.randomUUID()));
  }

  @Test
  void getUsers_success_returnsList() {
    RestTemplate rt = mock(RestTemplate.class);
    PermissionClient client = new PermissionClient(rt, "http://permission");

    PermissionUserDto u1 = new PermissionUserDto(UUID.randomUUID(), "A", "a@a.com");
    PermissionUserDto u2 = new PermissionUserDto(UUID.randomUUID(), "B", "b@b.com");

    when(rt.exchange(
            eq("http://permission/api/users"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)))
        .thenReturn(new ResponseEntity<>(List.of(u1, u2), HttpStatus.OK));

    List<PermissionUserDto> res = client.getUsers("tok");

    assertEquals(2, res.size());
    assertEquals("A", res.get(0).name());
  }

  @Test
  void getUsers_whenNon2xxOrNullBody_throws() {
    RestTemplate rt = mock(RestTemplate.class);
    PermissionClient client = new PermissionClient(rt, "http://permission");

    when(rt.exchange(
            eq("http://permission/api/users"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)))
        .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

    assertThrows(IllegalStateException.class, () -> client.getUsers("tok"));

    when(rt.exchange(
            eq("http://permission/api/users"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class)))
        .thenReturn(new ResponseEntity<>(null, HttpStatus.FORBIDDEN));

    assertThrows(IllegalStateException.class, () -> client.getUsers("tok"));
  }
}
