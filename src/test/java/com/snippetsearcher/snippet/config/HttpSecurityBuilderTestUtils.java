package com.snippetsearcher.snippet.config;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

public final class HttpSecurityBuilderTestUtils {

  private HttpSecurityBuilderTestUtils() {}

  public static HttpSecurity httpSecurity() {
    AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
    ctx.register(TestSecurityConfig.class);
    ctx.refresh();

    return ctx.getBean(HttpSecurity.class);
  }

  @EnableWebSecurity
  static class TestSecurityConfig {
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
      return http.build();
    }
  }
}
