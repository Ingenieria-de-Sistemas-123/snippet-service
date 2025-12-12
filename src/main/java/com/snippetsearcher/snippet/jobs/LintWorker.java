package com.snippetsearcher.snippet.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippetsearcher.snippet.client.language.LanguageClient;
import com.snippetsearcher.snippet.dto.LanguageDtos;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.model.SnippetComplianceStatus;
import com.snippetsearcher.snippet.repository.SnippetRepository;
import com.snippetsearcher.snippet.service.LintIssueFilter;
import com.snippetsearcher.snippet.service.LintingRulesService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class LintWorker {

  private static final Logger log = LoggerFactory.getLogger(LintWorker.class);
  private static final String LINT_QUEUE = "lint-jobs";

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;
  private final SnippetRepository snippetRepository;
  private final com.snippetsearcher.snippet.client.AssetClient assetClient;
  private final LanguageClient languageClient;
  private final LintingRulesService lintingRulesService;
  private final LintIssueFilter lintIssueFilter;

  public LintWorker(
      RedisTemplate<String, String> redisTemplate,
      ObjectMapper objectMapper,
      SnippetRepository snippetRepository,
      com.snippetsearcher.snippet.client.AssetClient assetClient,
      LanguageClient languageClient,
      LintingRulesService lintingRulesService,
      LintIssueFilter lintIssueFilter) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
    this.snippetRepository = snippetRepository;
    this.assetClient = assetClient;
    this.languageClient = languageClient;
    this.lintingRulesService = lintingRulesService;
    this.lintIssueFilter = lintIssueFilter;
  }

  @Scheduled(fixedDelay = 2000)
  public void pollQueue() {
    String json = redisTemplate.opsForList().rightPop(LINT_QUEUE);
    if (json == null) return;

    try {
      JobPayload payload = objectMapper.readValue(json, JobPayload.class);
      if (payload.type() != JobType.LINT_ALL_SNIPPETS) {
        log.warn("Job inesperado en cola LINT: {}", payload);
        return;
      }
      runLintForAdmin(payload.adminId());
    } catch (Exception e) {
      log.error("Error procesando job de linting: {}", json, e);
    }
  }

  private void runLintForAdmin(UUID adminId) {
    log.info("Ejecutando lint masivo para admin {}", adminId);
    List<Snippet> snippets = snippetRepository.findAllByOwnerUserId(adminId);
    for (Snippet snippet : snippets) {
      try {
        byte[] data = assetClient.downloadSnippet(snippet.getAssetKey());
        String content = new String(data, StandardCharsets.UTF_8);

        LanguageDtos.AnalyzeResponse res =
            languageClient.analyze(
                new LanguageDtos.AnalyzeRequest(
                    snippet.getLanguage(), snippet.getVersion(), content));

        List<LanguageDtos.AnalyzeIssue> filtered =
            lintIssueFilter.filter(res.issues(), lintingRulesService.getLintingRules());

        if (!filtered.isEmpty()) {
          log.info(
              "Lint issues for snippet {} after filtering active rules: {}",
              snippet.getId(),
              filtered.size());
        }

        applyComplianceResult(snippet, filtered);

      } catch (Exception ex) {
        log.error("Error linteando snippet {}. Se continúa.", snippet.getId(), ex);
      }
    }
  }

  private void applyComplianceResult(Snippet snippet, List<LanguageDtos.AnalyzeIssue> issues) {
    SnippetComplianceStatus newStatus =
        issues == null || issues.isEmpty()
            ? SnippetComplianceStatus.VALID
            : SnippetComplianceStatus.INVALID;
    String newMessage = issues == null || issues.isEmpty() ? null : issues.getFirst().message();

    if (newStatus == snippet.getComplianceStatus()
        && Objects.equals(newMessage, snippet.getComplianceMessage())) {
      return;
    }

    snippet.setComplianceStatus(newStatus);
    snippet.setComplianceMessage(newMessage);
    snippetRepository.save(snippet);
  }
}
