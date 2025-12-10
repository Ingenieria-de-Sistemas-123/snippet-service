package com.snippetsearcher.snippet.service;

import com.snippetsearcher.snippet.dto.request.RuleRequest;
import com.snippetsearcher.snippet.dto.response.RuleResponse;
import com.snippetsearcher.snippet.model.RuleEntity;
import com.snippetsearcher.snippet.model.RuleType;
import com.snippetsearcher.snippet.repository.RuleRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Deprecated: legacy persistence of rules in DB. Prefer {@link FormattingRulesService} and
 * {@link com.snippetsearcher.snippet.service.LintingRulesService} which back rules in asset-service
 * and trigger the appropriate jobs.
 */
@Deprecated
@Service
public class RuleService {

  private final RuleRepository ruleRepository;

  public RuleService(RuleRepository ruleRepository) {
    this.ruleRepository = ruleRepository;
  }

  @Transactional(readOnly = true)
  public List<RuleResponse> getRules(RuleType type) {
    return ruleRepository.findByTypeOrderByNameAsc(type).stream()
        .map(RuleService::toResponse)
        .toList();
  }

  @Transactional
  public List<RuleResponse> replaceRules(RuleType type, List<RuleRequest> requests) {
    ruleRepository.deleteByType(type);
    List<RuleEntity> entities =
        requests.stream()
            .map(req -> new RuleEntity(req.id(), req.name(), type, req.isActive(), req.value()))
            .toList();
    return ruleRepository.saveAll(entities).stream().map(RuleService::toResponse).toList();
  }

  private static RuleResponse toResponse(RuleEntity entity) {
    return new RuleResponse(entity.getId(), entity.getName(), entity.isActive(), entity.getValue());
  }
}
