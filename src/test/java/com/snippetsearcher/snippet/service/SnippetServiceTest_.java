package com.snippetsearcher.snippet.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.snippetsearcher.snippet.client.AssetClient;
import com.snippetsearcher.snippet.client.PermissionClient;
import com.snippetsearcher.snippet.client.language.LanguageClient;
import com.snippetsearcher.snippet.dto.*;
import com.snippetsearcher.snippet.dto.LanguageDtos;
import com.snippetsearcher.snippet.dto.request.CreateSnippetRequest;
import com.snippetsearcher.snippet.dto.request.ShareSnippetRequest;
import com.snippetsearcher.snippet.dto.response.*;
import com.snippetsearcher.snippet.jobs.SnippetJobProducer;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.model.SnippetComplianceStatus;
import com.snippetsearcher.snippet.model.SnippetTest;
import com.snippetsearcher.snippet.repository.SnippetRepository;
import com.snippetsearcher.snippet.repository.SnippetTestRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestClientException;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class SnippetServiceTest_ {

  @Mock SnippetRepository snippetRepository;
  @Mock AssetClient assetClient;
  @Mock PermissionClient permissionClient;
  @Mock LanguageClient languageClient;
  @Mock SnippetTestRepository snippetTestRepository;
  @Mock SnippetJobProducer jobProducer;
  @Mock LintingRulesService lintingRulesService;
  @Mock LintIssueFilter lintIssueFilter;

  SnippetService service;

  @BeforeEach
  void setUp() {
    service =
        new SnippetService(
            snippetRepository,
            assetClient,
            permissionClient,
            languageClient,
            snippetTestRepository,
            jobProducer,
            lintingRulesService,
            lintIssueFilter,
            "snippets");
  }

  // ---------------- helpers ----------------

  private Jwt jwtWithClaims(String token, String sub, String email, String name) {
    return Jwt.withTokenValue(token)
        .header("alg", "none")
        .claim("sub", sub)
        .claim("email", email)
        .claim("name", name)
        .build();
  }

  private Snippet mkSnippet(UUID id, UUID ownerId, String assetKey) {
    // usa el constructor real que ya tenés en el service
    Snippet s = new Snippet("n", "PrintScript", "1.0", "d", assetKey, ownerId);
    // si tu entidad tiene setId(), setealo; si no, mockea repository.save para devolverlo con id
    try {
      var m = s.getClass().getMethod("setId", UUID.class);
      m.invoke(s, id);
    } catch (Exception ignored) {
      // si no existe setId, no pasa nada: igual la mayoría de tests no dependen del id interno
    }
    s.setComplianceStatus(SnippetComplianceStatus.VALID);
    s.setComplianceMessage(null);
    return s;
  }

  // ---------------- getSnippet ----------------

  @Test
  void getSnippet_returns_snippet_with_content_lint_and_tests_and_updates_compliance() {
    Jwt jwt = jwtWithClaims("t", "auth0|abc", "a@b.com", "Toto");
    UUID userId = UUID.randomUUID();
    UUID snippetId = UUID.randomUUID();
    UUID testId = UUID.randomUUID();

    // ensureUser OK
    when(permissionClient.ensureUser("t"))
        .thenReturn(new UserAccountDto(userId, "auth0|abc", "a@b.com", "Toto", null));

    // permiso ok
    when(permissionClient.listSnippetPermissions("t"))
        .thenReturn(List.of(new SnippetPermissionDto(snippetId, PermissionTypeDto.OWNER)));

    Snippet snippet = mkSnippet(snippetId, userId, "snippets/x.prs");
    when(snippetRepository.findById(snippetId)).thenReturn(java.util.Optional.of(snippet));

    // contenido
    when(assetClient.downloadSnippet("snippets/x.prs"))
        .thenReturn("let a: number = 1;".getBytes(StandardCharsets.UTF_8));

    // analyze + filter => 1 issue
    LanguageDtos.AnalyzeResponse analyzeResp =
        new LanguageDtos.AnalyzeResponse(
            List.of(
                new LanguageDtos.AnalyzeIssue(
                    null, "ya declarada previamente", "ERROR", 1, 2, 1, 3)),
            "raw");
    when(languageClient.analyze(any())).thenReturn(analyzeResp);

    when(lintingRulesService.getLintingRules()).thenReturn(List.of());
    when(lintIssueFilter.filter(anyList(), anyList()))
        .thenReturn(
            List.of(
                new LanguageDtos.AnalyzeIssue(
                    "no-duplicate-var", "ya declarada previamente", "ERROR", 1, 2, 1, 3)));

    // tests
    SnippetTest test = new SnippetTest();
    test.setId(testId);
    test.setName("t1");
    test.setDescription("d1");
    test.setLastRunAt(null);
    when(snippetTestRepository.findBySnippetId(any())).thenReturn(List.of(test));

    // cuando hay lint, aplica compliance INVALID y guarda
    SnippetResponse resp = service.getSnippet(jwt, snippetId);

    assertThat(resp.id()).isEqualTo(snippet.getId());
    assertThat(resp.content()).contains("let a");
    assertThat(resp.lintErrors()).hasSize(1);
    assertThat(resp.lintErrors().get(0).rule()).isEqualTo("no-duplicate-var");
    assertThat(resp.complianceStatus()).isEqualTo(SnippetComplianceStatus.INVALID);
    assertThat(resp.tests()).hasSize(1);

    verify(snippetRepository).save(snippet);
  }

  // ---------------- shareSnippet ----------------

  @Test
  void shareSnippet_throws_when_request_is_null_or_userId_null() {
    Jwt jwt = jwtWithClaims("t", "auth0|abc", "a@b.com", "Toto");
    UUID snippetId = UUID.randomUUID();

    assertThrows(IllegalArgumentException.class, () -> service.shareSnippet(jwt, snippetId, null));
    assertThrows(
        IllegalArgumentException.class,
        () -> service.shareSnippet(jwt, snippetId, new ShareSnippetRequest(null)));
  }

  @Test
  void shareSnippet_throws_when_target_is_owner() {
    Jwt jwt = jwtWithClaims("t", "auth0|abc", "a@b.com", "Toto");
    UUID userId = UUID.randomUUID();
    UUID snippetId = UUID.randomUUID();

    when(permissionClient.ensureUser("t"))
        .thenReturn(new UserAccountDto(userId, "auth0|abc", "a@b.com", "Toto", null));

    Snippet owned = mkSnippet(snippetId, userId, "snippets/a.prs");
    when(snippetRepository.findByIdAndOwnerUserId(snippetId, userId))
        .thenReturn(java.util.Optional.of(owned));

    assertThrows(
        IllegalArgumentException.class,
        () -> service.shareSnippet(jwt, snippetId, new ShareSnippetRequest(userId)));

    verify(permissionClient, never()).createSharedPermission(any(), any(), any());
  }

  @Test
  void shareSnippet_calls_permission_service_and_returns_snippet() {
    Jwt jwt = jwtWithClaims("t", "auth0|abc", "a@b.com", "Toto");
    UUID userId = UUID.randomUUID();
    UUID snippetId = UUID.randomUUID();
    UUID targetId = UUID.randomUUID();

    when(permissionClient.ensureUser("t"))
        .thenReturn(new UserAccountDto(userId, "auth0|abc", "a@b.com", "Toto", null));

    Snippet owned = mkSnippet(snippetId, userId, "snippets/a.prs");
    when(snippetRepository.findByIdAndOwnerUserId(snippetId, userId))
        .thenReturn(java.util.Optional.of(owned));

    SnippetResponse resp = service.shareSnippet(jwt, snippetId, new ShareSnippetRequest(targetId));

    assertThat(resp.id()).isEqualTo(owned.getId());
    verify(permissionClient).createSharedPermission("t", snippetId, targetId);
  }

  // ---------------- executeSnippet ----------------

  @Test
  void executeSnippet_downloads_content_and_calls_language_execute() {
    Jwt jwt = jwtWithClaims("t", "auth0|abc", "a@b.com", "Toto");
    UUID userId = UUID.randomUUID();
    UUID snippetId = UUID.randomUUID();

    when(permissionClient.ensureUser("t"))
        .thenReturn(new UserAccountDto(userId, "auth0|abc", "a@b.com", "Toto", null));

    Snippet owned = mkSnippet(snippetId, userId, "snippets/a.prs");
    when(snippetRepository.findByIdAndOwnerUserId(snippetId, userId))
        .thenReturn(java.util.Optional.of(owned));

    when(assetClient.downloadSnippet("snippets/a.prs"))
        .thenReturn("println(\"hi\");".getBytes(StandardCharsets.UTF_8));

    LanguageDtos.ExecuteResponse execResp = new LanguageDtos.ExecuteResponse(0, "out", "");
    when(languageClient.execute(any())).thenReturn(execResp);

    LanguageDtos.ExecuteResponse result = service.executeSnippet(jwt, snippetId, "input");

    assertThat(result.exitCode()).isEqualTo(0);
    assertThat(result.stdout()).isEqualTo("out");
    verify(languageClient)
        .execute(
            argThat(
                r ->
                    "PrintScript".equals(r.language())
                        && "1.0".equals(r.version())
                        && r.content().contains("println")
                        && "input".equals(r.input())));
  }

  // ---------------- deleteSnippet ----------------

  @Test
  void deleteSnippet_deletes_owned_snippet() {
    Jwt jwt = jwtWithClaims("t", "auth0|abc", "a@b.com", "Toto");
    UUID userId = UUID.randomUUID();
    UUID snippetId = UUID.randomUUID();

    when(permissionClient.ensureUser("t"))
        .thenReturn(new UserAccountDto(userId, "auth0|abc", "a@b.com", "Toto", null));

    Snippet owned = mkSnippet(snippetId, userId, "snippets/a.prs");
    when(snippetRepository.findByIdAndOwnerUserId(snippetId, userId))
        .thenReturn(java.util.Optional.of(owned));

    service.deleteSnippet(jwt, snippetId);

    verify(snippetRepository).delete(owned);
  }

  // ---------------- createSnippet + fallback ensureUser ----------------

  @Test
  void createSnippet_throws_when_file_missing() {
    Jwt jwt = jwtWithClaims("t", "auth0|abc", "a@b.com", "Toto");
    when(permissionClient.ensureUser("t"))
        .thenReturn(new UserAccountDto(UUID.randomUUID(), "auth0|abc", "a@b.com", "Toto", null));

    CreateSnippetRequest req = new CreateSnippetRequest("name", "PrintScript", "1.0", "desc");

    assertThrows(IllegalArgumentException.class, () -> service.createSnippet(jwt, req, null));
  }

  // ---------------- getFriends ----------------

  @Test
  void getFriends_returns_empty_when_permission_client_fails() {
    Jwt jwt = jwtWithClaims("t", "auth0|abc", "a@b.com", "Toto");
    UUID userId = UUID.randomUUID();

    when(permissionClient.ensureUser("t"))
        .thenReturn(new UserAccountDto(userId, "auth0|abc", "a@b.com", "Toto", null));
    when(permissionClient.getUsers("t")).thenThrow(new RestClientException("down"));

    List<FriendsResponse> res = service.getFriends(jwt);
    assertThat(res).isEmpty();
  }
}
