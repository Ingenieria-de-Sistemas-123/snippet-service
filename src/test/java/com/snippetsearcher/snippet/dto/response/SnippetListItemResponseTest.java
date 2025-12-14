package com.snippetsearcher.snippet.dto.response;

import static org.junit.jupiter.api.Assertions.*;

import com.snippetsearcher.snippet.dto.PermissionTypeDto;
import com.snippetsearcher.snippet.model.Snippet;
import com.snippetsearcher.snippet.model.SnippetComplianceStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SnippetListItemResponseTest {

  @Test
  void mapsEntityToDto() {
    UUID ownerId = UUID.randomUUID();
    Snippet snippet = new Snippet("name", "ps", "1.0", "desc", "key.prs", ownerId);
    snippet.setId(UUID.randomUUID());

    SnippetListItemResponse response =
        SnippetListItemResponse.fromEntity(snippet, "prs", PermissionTypeDto.OWNER);

    assertEquals(snippet.getId(), response.id());
    assertEquals("prs", response.extension());
    assertEquals(SnippetComplianceStatus.VALID, response.complianceStatus());
    assertTrue(response.valid());
    assertEquals(PermissionTypeDto.OWNER, response.relation());
  }
}
