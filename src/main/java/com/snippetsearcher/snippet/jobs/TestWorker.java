package com.snippetsearcher.snippet.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippetsearcher.snippet.client.language.LanguageClient;
import com.snippetsearcher.snippet.dto.LanguageDtos;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.model.SnippetTest;
import com.snippetsearcher.snippet.repository.SnippetRepository;
import com.snippetsearcher.snippet.repository.SnippetTestRepository;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TestWorker {

  private static final Logger log = LoggerFactory.getLogger(TestWorker.class);
  private static final String TEST_QUEUE = "test-jobs";

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;
  private final SnippetTestRepository snippetTestRepository;
  private final SnippetRepository snippetRepository;
  private final com.snippetsearcher.snippet.client.AssetClient assetClient;
  private final LanguageClient languageClient;

  public TestWorker(
      RedisTemplate<String, String> redisTemplate,
      ObjectMapper objectMapper,
      SnippetTestRepository snippetTestRepository,
      SnippetRepository snippetRepository,
      com.snippetsearcher.snippet.client.AssetClient assetClient,
      LanguageClient languageClient) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
    this.snippetTestRepository = snippetTestRepository;
    this.snippetRepository = snippetRepository;
    this.assetClient = assetClient;
    this.languageClient = languageClient;
  }

  @Scheduled(fixedDelay = 2000)
  public void pollQueue() {
    String json = redisTemplate.opsForList().rightPop(TEST_QUEUE);
    if (json == null) return;

    try {
      JobPayload payload = objectMapper.readValue(json, JobPayload.class);
      if (payload.type() != JobType.RUN_ALL_TESTS_FOR_SNIPPET) {
        log.warn("Job inesperado en cola TEST: {}", payload);
        return;
      }
      runAllTestsForSnippet(payload.snippetId());
    } catch (Exception e) {
      log.error("Error procesando job de tests: {}", json, e);
    }
  }

  private void runAllTestsForSnippet(UUID snippetId) {
    log.info("Ejecutando tests automáticos para snippet {}", snippetId);

    Snippet snippet =
        snippetRepository
            .findById(snippetId)
            .orElseThrow(() -> new IllegalArgumentException("Snippet no encontrado"));

    String snippetContent =
        new String(assetClient.downloadSnippet(snippet.getAssetKey()), StandardCharsets.UTF_8);

    List<SnippetTest> tests = snippetTestRepository.findBySnippetId(snippet.getId());
    for (SnippetTest test : tests) {
      try {
        String executableContent =
            snippetContent + System.lineSeparator() + System.lineSeparator() + test.getScript();

        LanguageDtos.ExecuteResponse response =
            languageClient.execute(
                new LanguageDtos.ExecuteRequest(
                    snippet.getLanguage(), snippet.getVersion(), executableContent, ""));

        test.setLastRunAt(OffsetDateTime.now());
        test.setLastRunExitCode(response.exitCode());
        test.setLastRunOutput(response.stdout());
        test.setLastRunError(response.stderr());

        snippetTestRepository.save(test);
      } catch (Exception ex) {
        log.error(
            "Error ejecutando test {} del snippet {}. Se continúa.", test.getId(), snippetId, ex);
      }
    }
  }
}
