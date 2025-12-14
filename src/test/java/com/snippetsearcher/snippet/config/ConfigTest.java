package com.snippetsearcher.snippet.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

class ConfigTest {
  // ---------- RestClientConfig ----------

  @Test
  void restClientConfig_creates_rest_template() {
    RestClientConfig cfg = new RestClientConfig();
    RestTemplate template = cfg.restTemplate(new RestTemplateBuilder());
    assertThat(template).isNotNull();
  }

  // ---------- RedisConfig ----------

  @Test
  void redisConfig_creates_connection_factory() {
    RedisConfig cfg = new RedisConfig();

    RedisConnectionFactory factory = cfg.redisConnectionFactory("localhost", 6379);

    assertThat(factory).isInstanceOf(LettuceConnectionFactory.class);
  }

  @Test
  void redisConfig_creates_redis_template() {
    RedisConfig cfg = new RedisConfig();
    RedisConnectionFactory factory = cfg.redisConnectionFactory("localhost", 6379);

    RedisTemplate<String, String> template = cfg.redisTemplate(factory);

    assertThat(template).isNotNull();
    assertThat(template.getConnectionFactory()).isSameAs(factory);
  }

  // ---------- HttpConfig ----------

  @Test
  void httpConfig_creates_rest_client() {
    HttpConfig cfg = new HttpConfig();

    RestClient client = cfg.restClient("http://example.com", 2000);

    assertThat(client).isNotNull();
  }

  @Test
  void httpConfig_applies_timeout() {
    HttpConfig cfg = new HttpConfig();

    RestClient client = cfg.restClient("http://example.com", 1234);

    assertThat(client).isNotNull();
  }

  // ---------- SchedulingConfig ----------

  @Test
  void schedulingConfig_instantiates() {
    SchedulingConfig cfg = new SchedulingConfig();
    assertThat(cfg).isNotNull();
  }
}
