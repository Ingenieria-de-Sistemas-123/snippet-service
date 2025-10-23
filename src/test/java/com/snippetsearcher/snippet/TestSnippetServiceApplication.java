package com.snippetsearcher.snippet;

import org.springframework.boot.SpringApplication;

public class TestSnippetServiceApplication {

  public static void main(String[] args) {
    SpringApplication.from(SnippetServiceApplication::main)
        .with(TestcontainersConfiguration.class)
        .run(args);
  }
}
