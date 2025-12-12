package com.snippetsearcher.snippet.jobs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class SnippetJobProducer {

  private static final Logger log = LoggerFactory.getLogger(SnippetJobProducer.class);

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  private static final String FORMAT_QUEUE = "format-jobs";
  private static final String LINT_QUEUE = "lint-jobs";
  private static final String TEST_QUEUE = "test-jobs";

  public SnippetJobProducer(
      RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
    this.redisTemplate = redisTemplate;
    this.objectMapper = objectMapper;
  }

  public void enqueueFormatAll(UUID adminId) {
    enqueue(FORMAT_QUEUE, new JobPayload(JobType.FORMAT_ALL_SNIPPETS, adminId, null));
  }

  public void enqueueLintAll(UUID adminId) {
    enqueue(LINT_QUEUE, new JobPayload(JobType.LINT_ALL_SNIPPETS, adminId, null));
  }

  public void enqueueRunAllTestsForSnippet(UUID snippetId) {
    enqueue(TEST_QUEUE, new JobPayload(JobType.RUN_ALL_TESTS_FOR_SNIPPET, null, snippetId));
  }

  private void enqueue(String queue, JobPayload payload) {
    try {
      String json = objectMapper.writeValueAsString(payload);
      redisTemplate.opsForList().leftPush(queue, json);
      log.info("Enqueued job {} on queue {}", payload, queue);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("No se pudo serializar job para Redis", e);
    }
  }
}
