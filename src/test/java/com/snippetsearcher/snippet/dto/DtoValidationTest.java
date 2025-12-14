package com.snippetsearcher.snippet.dto;

import static org.junit.jupiter.api.Assertions.*;

import com.snippetsearcher.snippet.dto.request.*;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DtoValidationTest {

  static Validator validator;

  @BeforeAll
  static void init() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  @Test
  void createSnippetRequest_blankName_invalid() {
    var dto = new CreateSnippetRequest(" ", null, "ps", "1.0");
    assertFalse(validator.validate(dto).isEmpty());
  }

  @Test
  void formatSnippetRequest_blankContent_invalid() {
    var dto = new FormatSnippetRequest(" ", "ps", "1.0", false);
    assertFalse(validator.validate(dto).isEmpty());
  }

  @Test
  void updateSnippetRequest_tooLongName_invalid() {
    String longName = "a".repeat(201);
    var dto = new UpdateSnippetRequest(longName, null, "ps", "1.0");
    assertFalse(validator.validate(dto).isEmpty());
  }

  @Test
  void shareSnippetRequest_nullUserId_invalid() {
    var dto = new ShareSnippetRequest(null);
    assertFalse(validator.validate(dto).isEmpty());
  }

  @Test
  void ruleRequest_blankId_invalid() {
    var dto = new RuleRequest(" ", "ok", true, 2);
    assertFalse(validator.validate(dto).isEmpty());
  }

  @Test
  void ruleRequest_ok_valid() {
    var dto = new RuleRequest("r1", "ok", true, 2);
    assertTrue(validator.validate(dto).isEmpty());
  }

  @Test
  void createSnippetTestRequest_blankExpectedOutput_invalid() {
    var dto = new CreateSnippetTestRequest("T", null, "input", " ");
    assertFalse(validator.validate(dto).isEmpty());
  }
}
