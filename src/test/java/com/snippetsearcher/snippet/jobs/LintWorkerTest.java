package com.snippetsearcher.snippet.jobs;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snippetsearcher.snippet.client.AssetClient;
import com.snippetsearcher.snippet.client.language.LanguageClient;
import com.snippetsearcher.snippet.dto.LanguageDtos;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.model.SnippetComplianceStatus;
import com.snippetsearcher.snippet.repository.SnippetRepository;
import com.snippetsearcher.snippet.service.LintIssueFilter;
import com.snippetsearcher.snippet.service.LintingRulesService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

class LintWorkerTest {

  @Test
  void pollQueue_updates_compliance_and_saves_when_changed() throws Exception {
    RedisTemplate<String, String> redis = mock(RedisTemplate.class);
    @SuppressWarnings("unchecked")
    ListOperations<String, String> ops = mock(ListOperations.class);
    when(redis.opsForList()).thenReturn(ops);

    UUID adminId = UUID.randomUUID();
    when(ops.rightPop("lint-jobs")).thenReturn("{json}");

    ObjectMapper mapper = mock(ObjectMapper.class);
    when(mapper.readValue(eq("{json}"), eq(JobPayload.class)))
        .thenReturn(new JobPayload(JobType.LINT_ALL_SNIPPETS, adminId, null));

    SnippetRepository repo = mock(SnippetRepository.class);
    AssetClient asset = mock(AssetClient.class);
    LanguageClient language = mock(LanguageClient.class);
    LintingRulesService rules = mock(LintingRulesService.class);
    LintIssueFilter filter = mock(LintIssueFilter.class);

    Snippet s = new Snippet("n", "printscript", null, null, "snippets/x.prs", adminId);
    s.setId(UUID.randomUUID());
    s.setComplianceStatus(SnippetComplianceStatus.UNKNOWN);
    when(repo.findAllByOwnerUserId(adminId)).thenReturn(List.of(s));

    when(asset.downloadSnippet("snippets/x.prs"))
        .thenReturn("content".getBytes(StandardCharsets.UTF_8));

    var issue = new LanguageDtos.AnalyzeIssue("no-duplicate-var", "bad", "ERROR", 1, 2, 1, 3);
    when(language.analyze(any()))
        .thenReturn(new LanguageDtos.AnalyzeResponse(List.of(issue), "raw"));
    when(filter.filter(anyList(), anyList())).thenReturn(List.of(issue));

    var worker = new LintWorker(redis, mapper, repo, asset, language, rules, filter);

    worker.pollQueue();

    verify(repo)
        .save(
            argThat(
                saved ->
                    saved.getComplianceStatus() == SnippetComplianceStatus.INVALID
                        && "bad".equals(saved.getComplianceMessage())));
  }
}
