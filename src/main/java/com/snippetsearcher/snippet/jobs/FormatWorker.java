package com.snippetsearcher.snippet.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippetsearcher.snippet.dto.LanguageDtos;
import com.snippetsearcher.snippet.language.LanguageClient;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.repository.SnippetRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class FormatWorker {

  private static final Logger log = LoggerFactory.getLogger(FormatWorker.class);
  private static final String FORMAT_QUEUE = "format-jobs";

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;
  private final SnippetRepository snippetRepository;
  private final com.snippetsearcher.snippet.client.AssetClient assetClient;
  private final LanguageClient languageClient;

  public FormatWorker(
      RedisTemplate<String, String> redisTemplate,
      ObjectMapper objectMapper,
      SnippetRepository snippetRepository,
      com.snippetsearcher.snippet.client.AssetClient assetClient,
      LanguageClient languageClient) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
    this.snippetRepository = snippetRepository;
    this.assetClient = assetClient;
    this.languageClient = languageClient;
  }

  @Scheduled(fixedDelay = 2000)
  public void pollQueue() {
    String json = redisTemplate.opsForList().rightPop(FORMAT_QUEUE);
    if (json == null) return;

    try {
      JobPayload payload = objectMapper.readValue(json, JobPayload.class);
      if (payload.type() != JobType.FORMAT_ALL_SNIPPETS) {
        log.warn("Job inesperado en cola FORMAT: {}", payload);
        return;
      }
      runFormatForAdmin(payload.adminId());
    } catch (Exception e) {
      log.error("Error procesando job de formato: {}", json, e);
    }
  }

  private void runFormatForAdmin(UUID adminId) {
    log.info("Ejecutando formateo masivo para admin {}", adminId);
    List<Snippet> snippets = snippetRepository.findAllByOwnerUserId(adminId);
    if (CollectionUtils.isEmpty(snippets)) return;

    for (Snippet snippet : snippets) {
      try {
        byte[] data = assetClient.downloadSnippet(snippet.getAssetKey());
        String content = new String(data, StandardCharsets.UTF_8);

        LanguageDtos.FormatResponse res =
            languageClient.format(
                new LanguageDtos.FormatRequest(
                    snippet.getLanguage(), snippet.getVersion(), content, false));

        byte[] formattedBytes = res.formatted().getBytes(StandardCharsets.UTF_8);
        String newKey =
            assetClient.uploadSnippet(
                "snippets", snippet.getId() + "-fmt.prs", formattedBytes, "text/plain");
        snippet.setAssetKey(newKey);
        snippetRepository.save(snippet);

      } catch (Exception ex) {
        log.error(
            "Error formateando snippet {}. Se continúa con el siguiente.", snippet.getId(), ex);
      }
    }
  }
}
