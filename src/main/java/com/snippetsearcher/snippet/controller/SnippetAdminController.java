package com.snippetsearcher.snippet.controller;

import com.snippetsearcher.snippet.client.PermissionClient;
import com.snippetsearcher.snippet.dto.UserAccountDto;
import com.snippetsearcher.snippet.jobs.SnippetJobProducer;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;

@RestController
@RequestMapping("/api/admin/snippets")
public class SnippetAdminController {

  private final SnippetJobProducer jobProducer;
  private final PermissionClient permissionClient;

  public SnippetAdminController(SnippetJobProducer jobProducer, PermissionClient permissionClient) {
    this.jobProducer = jobProducer;
    this.permissionClient = permissionClient;
  }

  // UC: Formateo masivo
  @PostMapping("/format")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void formatAllSnippets(@AuthenticationPrincipal Jwt jwt) {
    UserAccountDto user = ensureUser(jwt);
    jobProducer.enqueueFormatAll(user.id());
  }

  // UC: Lint masivo
  @PostMapping("/lint")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void lintAllSnippets(@AuthenticationPrincipal Jwt jwt) {
    UserAccountDto user = ensureUser(jwt);
    jobProducer.enqueueLintAll(user.id());
  }

  // --- Helper: mismo criterio que en SnippetService.ensureUser/buildUserFromJwt ---

  private UserAccountDto ensureUser(Jwt jwt) {
    try {
      return permissionClient.ensureUser(jwt.getTokenValue());
    } catch (RestClientException | IllegalStateException ex) {
      String subject = jwt.getClaimAsString("sub");
      if (!StringUtils.hasText(subject)) {
        subject = jwt.getSubject();
      }
      if (!StringUtils.hasText(subject)) {
        subject = jwt.getClaimAsString("email");
      }
      if (!StringUtils.hasText(subject)) {
        subject = "anonymous";
      }

      UUID userId = UUID.nameUUIDFromBytes(subject.getBytes(StandardCharsets.UTF_8));
      String email = jwt.getClaimAsString("email");
      String name = jwt.getClaimAsString("name");
      String picture = jwt.getClaimAsString("picture");
      return new UserAccountDto(userId, subject, email, name, picture);
    }
  }
}
