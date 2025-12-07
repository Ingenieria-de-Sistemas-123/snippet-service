package com.snippetsearcher.snippet.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "rules")
public class RuleEntity {

  @Id
  @Column(length = 36)
  private String id;

  @Column(nullable = false, length = 120)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private RuleType type;

  @Column(name = "is_active", nullable = false)
  private boolean active;

  @Column
  private Integer value;

  public RuleEntity() {}

  public RuleEntity(String id, String name, RuleType type, boolean active, Integer value) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.active = active;
    this.value = value;
  }

}
