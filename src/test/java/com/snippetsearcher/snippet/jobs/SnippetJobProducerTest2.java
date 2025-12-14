package com.snippetsearcher.snippet.jobs;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

class SnippetJobProducerTest2 {

  @Test
  void enqueue_format_job() {
    RedisTemplate<String, String> redis = mock(RedisTemplate.class);
    ListOperations<String, String> ops = mock(ListOperations.class);
    when(redis.opsForList()).thenReturn(ops);

    var producer = new SnippetJobProducer(redis, new ObjectMapper());

    producer.enqueueFormatAll(UUID.randomUUID());

    verify(ops).leftPush(eq("format-jobs"), anyString());
  }

  @Test
  void enqueue_throws_when_serialization_fails() throws Exception {
    RedisTemplate<String, String> redis = mock(RedisTemplate.class);
    ObjectMapper mapper = mock(ObjectMapper.class);

    // 👇 FORZAMOS la excepción correcta
    when(mapper.writeValueAsString(any()))
        .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("boom") {});

    var producer = new SnippetJobProducer(redis, mapper);

    assertThrows(IllegalStateException.class, () -> producer.enqueueLintAll(UUID.randomUUID()));
  }

  @Test
  void enqueue_format_throws_when_serialization_fails() throws Exception {
    RedisTemplate<String, String> redis = mock(RedisTemplate.class);
    ObjectMapper mapper = mock(ObjectMapper.class);

    when(mapper.writeValueAsString(any()))
        .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("boom") {});

    var producer = new SnippetJobProducer(redis, mapper);

    assertThrows(IllegalStateException.class, () -> producer.enqueueFormatAll(UUID.randomUUID()));
  }

  @Test
  void enqueue_run_tests_throws_when_serialization_fails() throws Exception {
    RedisTemplate<String, String> redis = mock(RedisTemplate.class);
    ObjectMapper mapper = mock(ObjectMapper.class);

    when(mapper.writeValueAsString(any()))
        .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("boom") {});

    var producer = new SnippetJobProducer(redis, mapper);

    assertThrows(
        IllegalStateException.class,
        () -> producer.enqueueRunAllTestsForSnippet(UUID.randomUUID()));
  }
}
