package com.snippetsearcher.snippet.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.snippetsearcher.snippet.client.AssetClient;
import com.snippetsearcher.snippet.client.PermissionClient;
import com.snippetsearcher.snippet.client.language.LanguageClient;
import com.snippetsearcher.snippet.dto.LanguageDtos;
import com.snippetsearcher.snippet.dto.PermissionTypeDto;
import com.snippetsearcher.snippet.dto.SnippetPermissionDto;
import com.snippetsearcher.snippet.dto.UserAccountDto;
import com.snippetsearcher.snippet.dto.request.CreateSnippetRequest;
import com.snippetsearcher.snippet.dto.request.ListSnippetsQuery;
import com.snippetsearcher.snippet.dto.request.UpdateSnippetRequest;
import com.snippetsearcher.snippet.dto.response.ListSnippetsResponse;
import com.snippetsearcher.snippet.dto.response.SnippetResponse;
import com.snippetsearcher.snippet.dto.response.SnippetTestExecutionResponse;
import com.snippetsearcher.snippet.exception.SnippetNotFoundException;
import com.snippetsearcher.snippet.jobs.SnippetJobProducer;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.model.SnippetComplianceStatus;
import com.snippetsearcher.snippet.model.SnippetTest;
import com.snippetsearcher.snippet.repository.SnippetRepository;
import com.snippetsearcher.snippet.repository.SnippetTestRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SnippetServiceTest {

  @Mock private SnippetRepository snippetRepository;
  @Mock private AssetClient assetClient;
  @Mock private PermissionClient permissionClient;
  @Mock private LanguageClient languageClient;
  @Mock private SnippetTestRepository snippetTestRepository;
  @Mock private SnippetJobProducer jobProducer;
  @Mock private LintingRulesService lintingRulesService;
  @Mock private LintIssueFilter lintIssueFilter;

  private SnippetService snippetService;

  @BeforeEach
  void setup() {
    snippetService =
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

    when(lintingRulesService.getLintingRules()).thenReturn(List.of());
    when(lintIssueFilter.filter(anyList(), anyList())).thenReturn(List.of());
    when(languageClient.analyze(any()))
        .thenReturn(new LanguageDtos.AnalyzeResponse(List.of(), "raw"));
    when(snippetRepository.save(any()))
        .thenAnswer(
            invocation -> {
              Snippet s = invocation.getArgument(0);
              if (s.getId() == null) {
                s.setId(UUID.randomUUID());
              }
              return s;
            });
    when(assetClient.uploadSnippet(anyString(), anyString(), any(), any()))
        .thenAnswer(invocation -> invocation.getArgument(1));
    when(permissionClient.ensureUser(any()))
        .thenReturn(new UserAccountDto(UUID.randomUUID(), "sub", "mail", "name", null));
  }

  @Test
  void createSnippetUploadsSanitizedFileAndRegistersOwner() {
    CreateSnippetRequest request =
        new CreateSnippetRequest("My Snippet", " desc  ", "ps", " unspecified ");
    MultipartFile file =
        new MockMultipartFile("file", "bad name?.prs", "text/plain", "print(1);".getBytes());

    SnippetResponse response = snippetService.createSnippet(jwt(), request, file);

    assertNotNull(response.id());
    assertNull(response.version(), "unspecified version should become null");
    assertEquals("desc", response.description());
    assertEquals(SnippetComplianceStatus.VALID, response.complianceStatus());

    ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
    verify(assetClient).uploadSnippet(eq("snippets"), keyCaptor.capture(), any(), eq("text/plain"));
    assertTrue(
        keyCaptor.getValue().contains("bad_name_.prs"),
        "filename should be sanitized inside the generated key");
    verify(permissionClient).createOwnerPermission(anyString(), any(), eq(response.id()));
  }

  @Test
  void createSnippetRejectsLintErrors() {
    when(languageClient.analyze(any()))
        .thenReturn(
            new LanguageDtos.AnalyzeResponse(
                List.of(new LanguageDtos.AnalyzeIssue("rule1", "msg", null, 1, 2, 1, 3)), "raw"));
    when(lintIssueFilter.filter(anyList(), anyList()))
        .thenReturn(List.of(new LanguageDtos.AnalyzeIssue("rule1", "msg", null, 1, 2, 1, 3)));

    MultipartFile file =
        new MockMultipartFile("file", "script.prs", "text/plain", "print(1);".getBytes());

    assertThrows(
        IllegalArgumentException.class,
        () ->
            snippetService.createSnippet(
                jwt(), new CreateSnippetRequest("Name", "d", "ps", "1.0"), file));
    verify(assetClient, never()).uploadSnippet(anyString(), anyString(), any(), any());
  }

  @Test
  void updateSnippetKeepsDescriptionWhenMissingAndEnqueuesTests() {
    UUID snippetId = UUID.randomUUID();
    Snippet existing =
        new Snippet("Old", "ps", "unspecified", "keep me", "asset", UUID.randomUUID());
    existing.setId(snippetId);

    when(snippetRepository.findByIdAndOwnerUserId(eq(snippetId), any()))
        .thenReturn(Optional.of(existing));

    MultipartFile file =
        new MockMultipartFile("file", "script.prs", "text/plain", "print(2);".getBytes());

    SnippetResponse response =
        snippetService.updateSnippet(
            jwt(), snippetId, new UpdateSnippetRequest("New", null, "ps", null), file);

    assertEquals("New", response.name());
    assertEquals("keep me", response.description(), "existing description should remain");
    assertNull(response.version(), "unspecified version should normalize to null");
    verify(jobProducer).enqueueRunAllTestsForSnippet(snippetId);
  }

  @Test
  void executeSnippetTestUpdatesResultAndReturnsOutput() {
    UUID snippetId = UUID.randomUUID();
    Snippet snippet = new Snippet("n", "ps", "1.0", "d", "asset-key", UUID.randomUUID());
    snippet.setId(snippetId);
    when(snippetRepository.findByIdAndOwnerUserId(eq(snippetId), any()))
        .thenReturn(Optional.of(snippet));

    SnippetTest test = new SnippetTest();
    test.setId(UUID.randomUUID());
    test.setSnippet(snippet);
    test.setScript("assert(true);");
    when(snippetTestRepository.findByIdAndSnippetId(eq(test.getId()), eq(snippetId)))
        .thenReturn(Optional.of(test));

    when(assetClient.downloadSnippet("asset-key")).thenReturn("print(1);".getBytes());
    when(languageClient.execute(any())).thenReturn(new LanguageDtos.ExecuteResponse(0, "ok", ""));

    SnippetTestExecutionResponse response =
        snippetService.executeSnippetTest(jwt(), snippetId, test.getId());

    assertTrue(response.passed());
    assertEquals(0, response.exitCode());
    verify(snippetTestRepository).save(argThat(t -> t.getLastRunExitCode() == 0));
  }

  @Test
  void executeSnippetTestRequiresScript() {
    UUID snippetId = UUID.randomUUID();
    Snippet snippet = new Snippet("n", "ps", "1.0", "d", "asset-key", UUID.randomUUID());
    snippet.setId(snippetId);
    when(snippetRepository.findByIdAndOwnerUserId(eq(snippetId), any()))
        .thenReturn(Optional.of(snippet));

    SnippetTest test = new SnippetTest();
    test.setId(UUID.randomUUID());
    test.setSnippet(snippet);
    test.setScript("  "); // missing script
    when(snippetTestRepository.findByIdAndSnippetId(eq(test.getId()), eq(snippetId)))
        .thenReturn(Optional.of(test));

    when(assetClient.downloadSnippet("asset-key")).thenReturn("print(1);".getBytes());

    assertThrows(
        IllegalArgumentException.class,
        () -> snippetService.executeSnippetTest(jwt(), snippetId, test.getId()));
    verify(languageClient, never()).execute(any());
  }

  @Test
  void ensureUserFallsBackToJwtWhenPermissionServiceFails() {
    when(permissionClient.ensureUser(any()))
        .thenThrow(new RestClientException("downstream unavailable") {});

    Jwt jwt =
        Jwt.withTokenValue("token")
            .header("alg", "none")
            .subject("subject-abc")
            .claim("email", "abc@test.com")
            .claim("name", "Example")
            .build();

    UserAccountDto user = snippetService.ensureUserForController(jwt);

    assertEquals("subject-abc", user.auth0Sub());
    assertEquals(UUID.nameUUIDFromBytes("subject-abc".getBytes(StandardCharsets.UTF_8)), user.id());
  }

  @Test
  void listSnippetsCombinesOwnedAndShared() {
    UUID ownerId = UUID.randomUUID();
    UserAccountDto user = new UserAccountDto(ownerId, "sub", "mail", "name", null);
    when(permissionClient.ensureUser(any())).thenReturn(user);

    UUID sharedId = UUID.randomUUID();
    when(permissionClient.listSnippetPermissions(anyString()))
        .thenReturn(List.of(new SnippetPermissionDto(sharedId, PermissionTypeDto.SHARED)));

    Snippet owned =
        new Snippet("A", "ps", "1.0", "d", "snippets/one.prs", ownerId) {
          {
            setId(UUID.randomUUID());
          }
        };
    Snippet shared =
        new Snippet("B", "ps", "1.0", "d", "snippets/two.prs", UUID.randomUUID()) {
          {
            setId(sharedId);
          }
        };

    when(snippetRepository.findAll(
            any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(owned, shared)));

    ListSnippetsQuery query = ListSnippetsQuery.from(0, 10, null, null, null, "all", "name", "asc");

    ListSnippetsResponse response = snippetService.listSnippets(jwt(), query);

    assertEquals(2, response.snippets().size());
    assertEquals(PermissionTypeDto.OWNER, response.snippets().get(0).relation());
    assertEquals(PermissionTypeDto.SHARED, response.snippets().get(1).relation());
  }

  @Test
  void deleteSnippetThrowsWhenNotFound() {
    when(snippetRepository.findByIdAndOwnerUserId(any(), any())).thenReturn(Optional.empty());

    assertThrows(
        SnippetNotFoundException.class,
        () -> snippetService.deleteSnippet(jwt(), UUID.randomUUID()));
  }

  private Jwt jwt() {
    return Jwt.withTokenValue("token").header("alg", "none").claim("sub", "user-123").build();
  }
}
