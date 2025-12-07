package com.snippetsearcher.snippet.controller;

import com.snippetsearcher.snippet.dto.request.RuleRequest;
import com.snippetsearcher.snippet.dto.response.RuleResponse;
import com.snippetsearcher.snippet.model.RuleType;
import com.snippetsearcher.snippet.service.RuleService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rules")
public class RuleController {

  private final RuleService ruleService;

  public RuleController(RuleService ruleService) {
    this.ruleService = ruleService;
  }

  @GetMapping("/format")
  public List<RuleResponse> getFormatRules() {
    return ruleService.getRules(RuleType.FORMAT);
  }

  @GetMapping("/lint")
  public List<RuleResponse> getLintRules() {
    return ruleService.getRules(RuleType.LINT);
  }

  @PutMapping("/format")
  public List<RuleResponse> updateFormatRules(@RequestBody @Valid List<RuleRequest> requests) {
    return ruleService.replaceRules(RuleType.FORMAT, requests);
  }

  @PutMapping("/lint")
  public List<RuleResponse> updateLintRules(@RequestBody @Valid List<RuleRequest> requests) {
    return ruleService.replaceRules(RuleType.LINT, requests);
  }
}
