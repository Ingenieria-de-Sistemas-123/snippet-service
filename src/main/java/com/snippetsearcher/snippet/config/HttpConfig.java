package com.snippetsearcher.snippet.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@org.springframework.context.annotation.Configuration
public class HttpConfig {
  @Bean
  RestClient restClient(
      @Value("${language.base-url}") String baseUrl,
      @Value("${language.timeout-ms:4000}") int timeoutMs) {

    SimpleClientHttpRequestFactory rf = new SimpleClientHttpRequestFactory();
    rf.setConnectTimeout(Duration.ofMillis(timeoutMs));
    rf.setReadTimeout(Duration.ofMillis(timeoutMs));

    return RestClient.builder().baseUrl(baseUrl).requestFactory(rf).build();
  }
}
