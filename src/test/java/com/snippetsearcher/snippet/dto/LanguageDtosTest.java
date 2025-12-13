package com.snippetsearcher.snippet.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class LanguageDtosTest {

  @Test
  void validateRequest_recordAccessorsAndEquality() {
    LanguageDtos.ValidateRequest a = new LanguageDtos.ValidateRequest("ps", "1.0", "print(1);");
    LanguageDtos.ValidateRequest b = new LanguageDtos.ValidateRequest("ps", "1.0", "print(1);");
    LanguageDtos.ValidateRequest c = new LanguageDtos.ValidateRequest("ps", "1.0", "print(2);");

    assertEquals("ps", a.language());
    assertEquals("1.0", a.version());
    assertEquals("print(1);", a.content());

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());
    assertNotEquals(a, c);
    assertTrue(a.toString().contains("ValidateRequest"));
  }

  @Test
  void validateResponse_andValidationError_recordsWork() {
    LanguageDtos.ValidationError err =
        new LanguageDtos.ValidationError("ruleX", 10, 5, "bad stuff");
    LanguageDtos.ValidateResponse resp = new LanguageDtos.ValidateResponse(false, List.of(err));

    assertFalse(resp.valid());
    assertEquals(1, resp.errors().size());
    assertEquals("ruleX", resp.errors().get(0).rule());
    assertEquals(10, resp.errors().get(0).line());
    assertEquals(5, resp.errors().get(0).col());
    assertEquals("bad stuff", resp.errors().get(0).message());

    assertTrue(err.toString().contains("ValidationError"));
    assertTrue(resp.toString().contains("ValidateResponse"));
  }

  @Test
  void formatRequest_defaultsCanBeNull() {
    LanguageDtos.FormatRequest req =
        new LanguageDtos.FormatRequest("ps", "1.0", "print(1);", null, null);

    assertEquals("ps", req.language());
    assertEquals("1.0", req.version());
    assertEquals("print(1);", req.content());
    assertNull(req.check());
    assertNull(req.configJson());
  }

  @Test
  void formatResponse_analyzeResponse_executeResponse_recordsWork() {
    LanguageDtos.FormatResponse fr = new LanguageDtos.FormatResponse(true, "x", "diag");
    assertTrue(fr.changed());
    assertEquals("x", fr.formatted());
    assertEquals("diag", fr.diagnostics());

    LanguageDtos.AnalyzeIssue issue = new LanguageDtos.AnalyzeIssue("r", "m", "WARN", 1, 2, 3, 4);
    LanguageDtos.AnalyzeResponse ar = new LanguageDtos.AnalyzeResponse(List.of(issue), "{raw}");
    assertEquals(1, ar.issues().size());
    assertEquals("{raw}", ar.raw());
    assertEquals("WARN", ar.issues().get(0).severity());

    LanguageDtos.ExecuteRequest erq =
        new LanguageDtos.ExecuteRequest("ps", "1.0", "print(1);", "in");
    assertEquals("in", erq.input());

    LanguageDtos.ExecuteResponse ers = new LanguageDtos.ExecuteResponse(0, "out", "");
    assertEquals(0, ers.exitCode());
    assertEquals("out", ers.stdout());
    assertEquals("", ers.stderr());
  }
}
