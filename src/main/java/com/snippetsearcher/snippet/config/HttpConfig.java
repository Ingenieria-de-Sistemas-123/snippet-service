package com.snippetsearcher.snippet.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;

@org.springframework.context.annotation.Configuration
public class HttpConfig {
  @Bean
  @Qualifier("languageRestClient")
  RestClient languageRestClient(RestClient.Builder b, @Value("${language.base-url}") String baseUrl) {
    return b.baseUrl(baseUrl).build();
  }

  @Bean
  @Qualifier("permissionRestClient")
  RestClient permissionRestClient(RestClient.Builder b, @Value("${permission.base-url:http://localhost:8083}") String baseUrl) {
    return b.baseUrl(baseUrl).build();
  }
}
