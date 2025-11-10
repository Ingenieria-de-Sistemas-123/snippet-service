package com.snippetsearcher.snippet.health;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PermissionServiceHealthIndicator implements HealthIndicator {

    private final RestClient http;

    public PermissionServiceHealthIndicator(@Qualifier("permissionRestClient") RestClient http) {
        this.http = http;
    }

    @Override
    public Health health() {
        try {
            var resp = http.get()
                    .uri("/actuator/health")
                    .retrieve()
                    .toEntity(String.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                return Health.up().withDetail("status", resp.getStatusCode().value()).build();
            }
            return Health.down().withDetail("status", resp.getStatusCode().value()).build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
