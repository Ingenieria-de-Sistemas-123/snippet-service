package com.snippetsearcher.snippet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@org.springframework.context.annotation.Configuration
public class HttpConfig {
  @Bean
  RestClient restClient(RestClient.Builder b, @Value("${language.base-url}") String baseUrl) {
    return b.baseUrl(baseUrl).build();
  }
}
