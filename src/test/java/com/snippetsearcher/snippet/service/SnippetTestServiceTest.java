package com.snippetsearcher.snippet.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.snippetsearcher.snippet.client.AssetClient;
import com.snippetsearcher.snippet.client.PermissionClient;
import com.snippetsearcher.snippet.client.language.LanguageClient;
import com.snippetsearcher.snippet.dto.UserAccountDto;
import com.snippetsearcher.snippet.dto.request.CreateSnippetTestRequest;
import com.snippetsearcher.snippet.exception.SnippetNotFoundException;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.model.SnippetTest;
import com.snippetsearcher.snippet.repository.SnippetRepository;
import com.snippetsearcher.snippet.repository.SnippetTestRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SnippetTestServiceTest {

  @Mock private SnippetRepository snippetRepository;
  @Mock private SnippetTestRepository snippetTestRepository;
  @Mock private PermissionClient permissionClient;
  @Mock private AssetClient assetClient;
  @Mock private LanguageClient languageClient;

  private SnippetTestService service;

  @BeforeEach
  void setup() {
    service =
        new SnippetTestService(
            snippetRepository,
            snippetTestRepository,
            permissionClient,
            assetClient,
            languageClient);

    when(permissionClient.ensureUser(any()))
        .thenReturn(new UserAccountDto(UUID.randomUUID(), "sub", "email", "name", null));
    when(snippetRepository.findByIdAndOwnerUserId(any(), any()))
        .thenAnswer(
            inv -> {
              UUID id = inv.getArgument(0);
              UUID owner = inv.getArgument(1);
              Snippet snippet = new Snippet("n", "ps", "1.0", "d", "asset", owner);
              snippet.setId(id);
              return Optional.of(snippet);
            });
  }

  @Test
  void createSnippetTestPersistsAndReturnsDto() {
    UUID snippetId = UUID.randomUUID();
    when(snippetTestRepository.save(any()))
        .thenAnswer(
            inv -> {
              SnippetTest st = inv.getArgument(0);
              st.setId(UUID.randomUUID());
              return st;
            });

    var response =
        service.createSnippetTest(
            jwt(), snippetId, new CreateSnippetTestRequest("name", "print(1);"));

    assertNotNull(response.id());
    assertEquals("name", response.name());
    verify(snippetTestRepository).save(any());
  }

  @Test
  void createSnippetTestRejectsNullRequest() {
    assertThrows(
        IllegalArgumentException.class,
        () -> service.createSnippetTest(jwt(), UUID.randomUUID(), null));
    verify(snippetTestRepository, never()).save(any());
  }

  @Test
  void listSnippetTestsThrowsWhenSnippetMissing() {
    when(snippetRepository.findByIdAndOwnerUserId(any(), any())).thenReturn(Optional.empty());

    assertThrows(
        SnippetNotFoundException.class, () -> service.listSnippetTests(jwt(), UUID.randomUUID()));
  }

  @Test
  void ensureUserFallbacksWhenPermissionServiceFails() {
    when(permissionClient.ensureUser(any())).thenThrow(new RestClientException("down") {});

    Jwt jwt =
        Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("sub", "subj")
            .claim("email", "mail@test.com")
            .build();

    UUID snippetId = UUID.randomUUID();
    UUID fallbackOwner =
        UUID.nameUUIDFromBytes("subj".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    Snippet snippet = new Snippet("n", "ps", "1.0", "d", "asset", fallbackOwner);
    snippet.setId(snippetId);
    when(snippetRepository.findByIdAndOwnerUserId(snippetId, fallbackOwner))
        .thenReturn(Optional.of(snippet));
    when(snippetTestRepository.findBySnippetId(snippetId)).thenReturn(List.of());

    assertDoesNotThrow(() -> service.listSnippetTests(jwt, snippetId));
    verify(snippetRepository).findByIdAndOwnerUserId(snippetId, fallbackOwner);
  }

  private Jwt jwt() {
    return Jwt.withTokenValue("token").header("alg", "none").claim("sub", "user-123").build();
  }
}
