package com.snippetsearcher.snippet.jobs;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

@ExtendWith(MockitoExtension.class)
class SnippetJobProducerTest {

  @Mock private RedisTemplate<String, String> redisTemplate;
  @Mock private ListOperations<String, String> listOperations;

  private SnippetJobProducer producer;

  @BeforeEach
  void setup() {
    when(redisTemplate.opsForList()).thenReturn(listOperations);
    producer = new SnippetJobProducer(redisTemplate, new ObjectMapper());
  }

  @Test
  void enqueuesFormatJob() {
    UUID adminId = UUID.randomUUID();

    producer.enqueueFormatAll(adminId);

    ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
    verify(listOperations).leftPush(eq("format-jobs"), jsonCaptor.capture());
    assertTrue(jsonCaptor.getValue().contains(JobType.FORMAT_ALL_SNIPPETS.name()));
  }

  @Test
  void enqueuesLintJob() {
    UUID adminId = UUID.randomUUID();

    producer.enqueueLintAll(adminId);

    ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
    verify(listOperations).leftPush(eq("lint-jobs"), jsonCaptor.capture());
    assertTrue(jsonCaptor.getValue().contains(JobType.LINT_ALL_SNIPPETS.name()));
  }

  @Test
  void enqueuesRunTestsJob() {
    UUID snippetId = UUID.randomUUID();

    producer.enqueueRunAllTestsForSnippet(snippetId);

    ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
    verify(listOperations).leftPush(eq("test-jobs"), jsonCaptor.capture());
    assertTrue(jsonCaptor.getValue().contains(snippetId.toString()));
  }
}
