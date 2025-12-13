package com.snippetsearcher.snippet.dto.request;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SnippetRequestsValidationTest {

  private Validator validator;

  @BeforeEach
  void setUp() {
    validator = Validation.buildDefaultValidatorFactory().getValidator();
  }

  // ---------- CreateSnippetRequest ----------
  @Test
  void createSnippetRequest_valid_passes() {
    CreateSnippetRequest req = new CreateSnippetRequest("Name", "desc", "ps", "1.0");
    assertTrue(validator.validate(req).isEmpty());
  }

  @Test
  void createSnippetRequest_blankName_fails() {
    CreateSnippetRequest req = new CreateSnippetRequest(" ", "desc", "ps", "1.0");
    assertFalse(validator.validate(req).isEmpty());
  }

  @Test
  void createSnippetRequest_nameTooLong_fails() {
    CreateSnippetRequest req = new CreateSnippetRequest("a".repeat(201), "desc", "ps", "1.0");
    assertFalse(validator.validate(req).isEmpty());
  }

  @Test
  void createSnippetRequest_blankLanguage_fails() {
    CreateSnippetRequest req = new CreateSnippetRequest("Name", "desc", "  ", "1.0");
    assertFalse(validator.validate(req).isEmpty());
  }

  // ---------- UpdateSnippetRequest ----------
  @Test
  void updateSnippetRequest_valid_passes() {
    UpdateSnippetRequest req = new UpdateSnippetRequest("Name", "desc", "ps", "1.0");
    assertTrue(validator.validate(req).isEmpty());
  }

  @Test
  void updateSnippetRequest_blankName_fails() {
    UpdateSnippetRequest req = new UpdateSnippetRequest(" ", "desc", "ps", "1.0");
    assertFalse(validator.validate(req).isEmpty());
  }

  @Test
  void updateSnippetRequest_languageTooLong_fails() {
    UpdateSnippetRequest req = new UpdateSnippetRequest("Name", "desc", "a".repeat(51), "1.0");
    assertFalse(validator.validate(req).isEmpty());
  }

  // ---------- CreateSnippetTestRequest ----------
  @Test
  void createSnippetTestRequest_valid_passes() {
    CreateSnippetTestRequest req = new CreateSnippetTestRequest("Test 1", "print(1);");
    assertTrue(validator.validate(req).isEmpty());
  }

  @Test
  void createSnippetTestRequest_blankScript_fails() {
    CreateSnippetTestRequest req = new CreateSnippetTestRequest("Test 1", " ");
    assertFalse(validator.validate(req).isEmpty());
  }

  // ---------- UpdateSnippetTestRequest ----------
  @Test
  void updateSnippetTestRequest_valid_passes() {
    UpdateSnippetTestRequest req = new UpdateSnippetTestRequest("Test 1", "print(1);");
    assertTrue(validator.validate(req).isEmpty());
  }

  @Test
  void updateSnippetTestRequest_blankName_fails() {
    UpdateSnippetTestRequest req = new UpdateSnippetTestRequest(" ", "print(1);");
    assertFalse(validator.validate(req).isEmpty());
  }

  // ---------- FormatSnippetRequest ----------
  @Test
  void formatSnippetRequest_valid_allowsNullOptionalFields() {
    FormatSnippetRequest req = new FormatSnippetRequest("print(1);", null, null, null);
    assertTrue(validator.validate(req).isEmpty());
  }

  @Test
  void formatSnippetRequest_blankContent_fails() {
    FormatSnippetRequest req = new FormatSnippetRequest(" ", "ps", "1.0", false);
    assertFalse(validator.validate(req).isEmpty());
  }

  // ---------- RuleRequest ----------
  @Test
  void ruleRequest_valid_passes() {
    RuleRequest req = new RuleRequest("spaceAfterColon", "Espacio después de ':'", true, 2);
    assertTrue(validator.validate(req).isEmpty());
  }

  @Test
  void ruleRequest_blankId_fails() {
    RuleRequest req = new RuleRequest(" ", "Name", true, null);
    assertFalse(validator.validate(req).isEmpty());
  }

  @Test
  void ruleRequest_blankName_fails() {
    RuleRequest req = new RuleRequest("id", " ", true, null);
    assertFalse(validator.validate(req).isEmpty());
  }

  // ---------- ShareSnippetRequest ----------
  @Test
  void shareSnippetRequest_nullUserId_fails() {
    ShareSnippetRequest req = new ShareSnippetRequest(null);
    assertFalse(validator.validate(req).isEmpty());
  }

  @Test
  void shareSnippetRequest_jsonProperty_userId_isRecognized() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    UUID id = UUID.fromString("00000000-0000-0000-0000-000000000123");

    ShareSnippetRequest req =
        mapper.readValue("{\"userId\":\"" + id + "\"}", ShareSnippetRequest.class);

    assertEquals(id, req.userId());
    assertTrue(validator.validate(req).isEmpty());
  }
}
