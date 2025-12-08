package com.snippetsearcher.snippet.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "snippet_tests")
public class SnippetTest {

  @Id
  @GeneratedValue
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "snippet_id", nullable = false, columnDefinition = "uuid")
  private Snippet snippet;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(length = 1000)
  private String description;

  @Lob
  @Column(name = "script", nullable = false)
  private String script;

  @Column(name = "last_run_at")
  private OffsetDateTime lastRunAt;

  @Column(name = "last_run_exit_code")
  private Integer lastRunExitCode;

  @Lob
  @Column(name = "last_run_output")
  private String lastRunOutput;

  @Lob
  @Column(name = "last_run_error")
  private String lastRunError;
}
