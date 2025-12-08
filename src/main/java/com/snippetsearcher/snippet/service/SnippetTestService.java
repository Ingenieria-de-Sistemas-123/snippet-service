package com.snippetsearcher.snippet.service;

import com.snippetsearcher.snippet.client.AssetClient;
import com.snippetsearcher.snippet.client.PermissionClient;
import com.snippetsearcher.snippet.dto.LanguageDtos;
import com.snippetsearcher.snippet.dto.UserAccountDto;
import com.snippetsearcher.snippet.dto.request.CreateSnippetTestRequest;
import com.snippetsearcher.snippet.dto.request.UpdateSnippetTestRequest;
import com.snippetsearcher.snippet.dto.response.SnippetTestExecutionResponse;
import com.snippetsearcher.snippet.dto.response.SnippetTestResponse;
import com.snippetsearcher.snippet.exception.SnippetNotFoundException;
import com.snippetsearcher.snippet.language.LanguageClient;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.model.SnippetTest;
import com.snippetsearcher.snippet.repository.SnippetRepository;
import com.snippetsearcher.snippet.repository.SnippetTestRepository;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;

@Service
public class SnippetTestService {

  private final SnippetRepository snippetRepository;
  private final SnippetTestRepository snippetTestRepository;
  private final PermissionClient permissionClient;
  private final AssetClient assetClient;
  private final LanguageClient languageClient;

  public SnippetTestService(
      SnippetRepository snippetRepository,
      SnippetTestRepository snippetTestRepository,
      PermissionClient permissionClient,
      AssetClient assetClient,
      LanguageClient languageClient) {
    this.snippetRepository = snippetRepository;
    this.snippetTestRepository = snippetTestRepository;
    this.permissionClient = permissionClient;
    this.assetClient = assetClient;
    this.languageClient = languageClient;
  }

  // --------- UC8: listar tests de un snippet ---------

  @Transactional(readOnly = true)
  public List<SnippetTestResponse> listSnippetTests(Jwt jwt, UUID snippetId) {
    UserAccountDto user = ensureUser(jwt);
    Snippet snippet = loadSnippetOwnedBy(snippetId, user.id());

    return snippetTestRepository.findBySnippetId(snippet.getId()).stream()
        .map(SnippetTestResponse::fromEntity)
        .toList();
  }

  // --------- UC8: crear test ---------

  @Transactional
  public SnippetTestResponse createSnippetTest(
      Jwt jwt, UUID snippetId, CreateSnippetTestRequest request) {
    UserAccountDto user = ensureUser(jwt);
    Snippet snippet = loadSnippetOwnedBy(snippetId, user.id());

    if (request == null) {
      throw new IllegalArgumentException("El request del test es obligatorio.");
    }

    SnippetTest test = new SnippetTest();
    test.setSnippet(snippet);
    test.setName(request.name());
    test.setScript(request.script());
    test.setLastRunAt(null);
    test.setLastRunExitCode(null);
    test.setLastRunOutput(null);
    test.setLastRunError(null);

    test = snippetTestRepository.save(test);
    return SnippetTestResponse.fromEntity(test);
  }

  // --------- UC8: actualizar test ---------

  @Transactional
  public SnippetTestResponse updateSnippetTest(
      Jwt jwt, UUID snippetId, UUID testId, UpdateSnippetTestRequest request) {
    UserAccountDto user = ensureUser(jwt);
    Snippet snippet = loadSnippetOwnedBy(snippetId, user.id());

    SnippetTest test =
        snippetTestRepository
            .findByIdAndSnippetId(testId, snippet.getId())
            .orElseThrow(() -> new IllegalArgumentException("El test indicado no existe."));

    test.setName(request.name());
    test.setScript(request.script());

    test = snippetTestRepository.save(test);
    return SnippetTestResponse.fromEntity(test);
  }

  // --------- UC8: eliminar test ---------

  @Transactional
  public void deleteSnippetTest(Jwt jwt, UUID snippetId, UUID testId) {
    UserAccountDto user = ensureUser(jwt);
    Snippet snippet = loadSnippetOwnedBy(snippetId, user.id());

    SnippetTest test =
        snippetTestRepository
            .findByIdAndSnippetId(testId, snippet.getId())
            .orElseThrow(() -> new IllegalArgumentException("El test indicado no existe."));

    snippetTestRepository.delete(test);
  }

  // --------- UC9: ejecutar test ---------

  @Transactional
  public SnippetTestExecutionResponse executeSnippetTest(Jwt jwt, UUID snippetId, UUID testId) {
    UserAccountDto user = ensureUser(jwt);
    Snippet snippet = loadSnippetOwnedBy(snippetId, user.id());

    SnippetTest test =
        snippetTestRepository
            .findByIdAndSnippetId(testId, snippet.getId())
            .orElseThrow(() -> new IllegalArgumentException("El test indicado no existe."));

    String snippetContent = downloadSnippetContent(snippet);
    String executableContent = buildExecutableContent(snippetContent, test.getScript());

    LanguageDtos.ExecuteResponse response = executeTest(snippet, executableContent);
    updateTestResult(test, response);
    snippetTestRepository.save(test);

    boolean passed = response.exitCode() == 0;
    return new SnippetTestExecutionResponse(
        test.getId(),
        passed,
        response.exitCode(),
        response.stdout(),
        response.stderr(),
        test.getLastRunAt());
  }

  // ===================== Helpers copiados de SnippetService =====================

  private UserAccountDto ensureUser(Jwt jwt) {
    try {
      return permissionClient.ensureUser(jwt.getTokenValue());
    } catch (RestClientException | IllegalStateException ex) {
      // mismo fallback que en SnippetService
      String subject = jwt.getClaimAsString("sub");
      if (!StringUtils.hasText(subject)) {
        subject = jwt.getSubject();
      }
      if (!StringUtils.hasText(subject)) {
        subject = jwt.getClaimAsString("email");
      }
      if (!StringUtils.hasText(subject)) {
        subject = "anonymous";
      }

      UUID userId = UUID.nameUUIDFromBytes(subject.getBytes(StandardCharsets.UTF_8));
      String email = jwt.getClaimAsString("email");
      String name = jwt.getClaimAsString("name");
      String picture = jwt.getClaimAsString("picture");
      return new UserAccountDto(userId, subject, email, name, picture);
    }
  }

  private Snippet loadSnippetOwnedBy(UUID snippetId, UUID ownerId) {
    return snippetRepository
        .findByIdAndOwnerUserId(snippetId, ownerId)
        .orElseThrow(() -> new SnippetNotFoundException(snippetId));
  }

  private String downloadSnippetContent(Snippet snippet) {
    try {
      byte[] data = assetClient.downloadSnippet(snippet.getAssetKey());
      return new String(data, StandardCharsets.UTF_8);
    } catch (RestClientException | IllegalStateException ex) {
      throw new IllegalStateException("No se pudo obtener el contenido del snippet.", ex);
    }
  }

  private String buildExecutableContent(String snippetContent, String testScript) {
    if (!StringUtils.hasText(testScript)) {
      throw new IllegalArgumentException("El script del test es obligatorio.");
    }
    return snippetContent + System.lineSeparator() + System.lineSeparator() + testScript;
  }

  private LanguageDtos.ExecuteResponse executeTest(Snippet snippet, String executableContent) {
    try {
      return languageClient.execute(
          new LanguageDtos.ExecuteRequest(
              snippet.getLanguage(), snippet.getVersion(), executableContent));
    } catch (Exception ex) {
      throw new IllegalStateException("No se pudo ejecutar el test del snippet.", ex);
    }
  }

  private void updateTestResult(SnippetTest test, LanguageDtos.ExecuteResponse response) {
    test.setLastRunAt(OffsetDateTime.now());
    test.setLastRunExitCode(response.exitCode());
    test.setLastRunOutput(response.stdout());
    test.setLastRunError(response.stderr());
  }
}
