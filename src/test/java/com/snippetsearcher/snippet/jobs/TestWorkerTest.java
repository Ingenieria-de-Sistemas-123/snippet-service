package com.snippetsearcher.snippet.jobs;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippetsearcher.snippet.client.AssetClient;
import com.snippetsearcher.snippet.client.language.LanguageClient;
import com.snippetsearcher.snippet.dto.LanguageDtos;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.model.SnippetTest;
import com.snippetsearcher.snippet.repository.SnippetRepository;
import com.snippetsearcher.snippet.repository.SnippetTestRepository;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

class TestWorkerTest {

  @Test
  void pollQueue_executes_all_tests_and_persists_results() throws Exception {
    RedisTemplate<String, String> redis = mock(RedisTemplate.class);
    @SuppressWarnings("unchecked")
    ListOperations<String, String> ops = mock(ListOperations.class);
    when(redis.opsForList()).thenReturn(ops);

    UUID snippetId = UUID.randomUUID();
    when(ops.rightPop("test-jobs")).thenReturn("{json}");

    ObjectMapper mapper = mock(ObjectMapper.class);
    when(mapper.readValue(eq("{json}"), eq(JobPayload.class)))
        .thenReturn(new JobPayload(JobType.RUN_ALL_TESTS_FOR_SNIPPET, null, snippetId));

    SnippetRepository snippetRepo = mock(SnippetRepository.class);
    SnippetTestRepository testRepo = mock(SnippetTestRepository.class);
    AssetClient asset = mock(AssetClient.class);
    LanguageClient language = mock(LanguageClient.class);

    Snippet s = new Snippet("n", "printscript", null, null, "snippets/a.prs", UUID.randomUUID());
    s.setId(snippetId);
    when(snippetRepo.findById(snippetId)).thenReturn(Optional.of(s));

    when(asset.downloadSnippet("snippets/a.prs"))
        .thenReturn("let a: number = 1;".getBytes(StandardCharsets.UTF_8));

    SnippetTest t1 = new SnippetTest();
    t1.setName("t1");
    t1.setExpectedOutput("OK");
    when(testRepo.findBySnippetId(snippetId)).thenReturn(List.of(t1));

    when(language.execute(any())).thenReturn(new LanguageDtos.ExecuteResponse(0, "OK", ""));

    var worker = new TestWorker(redis, mapper, testRepo, snippetRepo, asset, language);

    worker.pollQueue();

    verify(testRepo)
        .save(
            argThat(
                saved ->
                    saved.getLastRunAt() != null
                        && Integer.valueOf(0).equals(saved.getLastRunExitCode())
                        && "OK".equals(saved.getLastRunOutput())));
  }

  @Test
  void pollQueue_when_snippet_missing_is_caught_and_does_not_save_tests() throws Exception {
    RedisTemplate<String, String> redis = mock(RedisTemplate.class);
    @SuppressWarnings("unchecked")
    ListOperations<String, String> ops = mock(ListOperations.class);
    when(redis.opsForList()).thenReturn(ops);

    UUID snippetId = UUID.randomUUID();
    when(ops.rightPop("test-jobs")).thenReturn("{json}");

    ObjectMapper mapper = mock(ObjectMapper.class);
    when(mapper.readValue(eq("{json}"), eq(JobPayload.class)))
        .thenReturn(new JobPayload(JobType.RUN_ALL_TESTS_FOR_SNIPPET, null, snippetId));

    SnippetRepository snippetRepo = mock(SnippetRepository.class);
    when(snippetRepo.findById(snippetId)).thenReturn(Optional.empty());

    SnippetTestRepository testRepo = mock(SnippetTestRepository.class);

    var worker =
        new TestWorker(
            redis,
            mapper,
            testRepo,
            snippetRepo,
            mock(AssetClient.class),
            mock(LanguageClient.class));

    worker.pollQueue();

    verify(testRepo, never()).save(any());
  }
}
