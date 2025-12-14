package com.snippetsearcher.snippet.jobs;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippetsearcher.snippet.client.AssetClient;
import com.snippetsearcher.snippet.client.language.LanguageClient;
import com.snippetsearcher.snippet.dto.LanguageDtos;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.repository.SnippetRepository;
import com.snippetsearcher.snippet.service.FormattingRulesService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

class FormatWorkerTest {

  @Test
  void pollQueue_ignores_unexpected_job_type() throws Exception {
    RedisTemplate<String, String> redis = mock(RedisTemplate.class);
    @SuppressWarnings("unchecked")
    ListOperations<String, String> ops = mock(ListOperations.class);
    when(redis.opsForList()).thenReturn(ops);

    ObjectMapper mapper = mock(ObjectMapper.class);
    when(ops.rightPop("format-jobs")).thenReturn("{json}");
    when(mapper.readValue(eq("{json}"), eq(JobPayload.class)))
        .thenReturn(new JobPayload(JobType.LINT_ALL_SNIPPETS, UUID.randomUUID(), null));

    SnippetRepository repo = mock(SnippetRepository.class);

    var worker =
        new FormatWorker(
            redis,
            mapper,
            repo,
            mock(AssetClient.class),
            mock(LanguageClient.class),
            mock(FormattingRulesService.class));

    worker.pollQueue();

    verifyNoInteractions(repo);
  }

  @Test
  void pollQueue_formats_all_snippets_for_admin() throws Exception {
    RedisTemplate<String, String> redis = mock(RedisTemplate.class);
    @SuppressWarnings("unchecked")
    ListOperations<String, String> ops = mock(ListOperations.class);
    when(redis.opsForList()).thenReturn(ops);

    UUID adminId = UUID.randomUUID();
    when(ops.rightPop("format-jobs")).thenReturn("{json}");

    ObjectMapper mapper = mock(ObjectMapper.class);
    when(mapper.readValue(eq("{json}"), eq(JobPayload.class)))
        .thenReturn(new JobPayload(JobType.FORMAT_ALL_SNIPPETS, adminId, null));

    SnippetRepository repo = mock(SnippetRepository.class);
    AssetClient asset = mock(AssetClient.class);
    LanguageClient language = mock(LanguageClient.class);
    FormattingRulesService rules = mock(FormattingRulesService.class);

    when(rules.buildFormatterConfigJsonFromStoredRules()).thenReturn("{\"indentSize\":2}");

    Snippet s = new Snippet("n", "printscript", null, null, "snippets/old.prs", adminId);
    s.setId(UUID.randomUUID());
    when(repo.findAllByOwnerUserId(adminId)).thenReturn(List.of(s));

    when(asset.downloadSnippet("snippets/old.prs"))
        .thenReturn("let a: number = 1;".getBytes(StandardCharsets.UTF_8));

    when(language.format(any()))
        .thenReturn(new LanguageDtos.FormatResponse(true, "formatted!", "diag"));

    when(asset.uploadSnippet(eq("snippets"), contains("-fmt.prs"), any(), eq("text/plain")))
        .thenReturn("snippets/new-fmt.prs");

    var worker = new FormatWorker(redis, mapper, repo, asset, language, rules);

    worker.pollQueue();

    verify(asset).downloadSnippet("snippets/old.prs");
    verify(asset).uploadSnippet(eq("snippets"), contains("-fmt.prs"), any(), eq("text/plain"));
    verify(repo).save(argThat(saved -> "snippets/new-fmt.prs".equals(saved.getAssetKey())));
  }
}
