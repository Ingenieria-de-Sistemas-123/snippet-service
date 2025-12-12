// src/main/java/com/snippetsearcher/snippet/rules/Rule.java
package com.snippetsearcher.snippet.rules;

public class Rule {

  private String id;
  private String name;
  private boolean active;
  private Integer value;

  public Rule() {}

  public Rule(String id, String name, boolean active, Integer value) {
    this.id = id;
    this.name = name;
    this.active = active;
    this.value = value;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public Integer getValue() {
    return value;
  }

  public void setValue(Integer value) {
    this.value = value;
  }
}
