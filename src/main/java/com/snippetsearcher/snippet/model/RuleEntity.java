package com.snippetsearcher.snippet.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public RuleType getType() {
    return type;
  }

  public boolean isActive() {
    return active;
  }

  public Integer getValue() {
    return value;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setType(RuleType type) {
    this.type = type;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public void setValue(Integer value) {
    this.value = value;
  }
}
