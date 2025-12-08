package com.snippetsearcher.snippet.service;

import com.snippetsearcher.snippet.dto.UserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.core.ParameterizedTypeReference;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
public class Auth0Service {

    private final WebClient client;
    private final String issuer;
    private final String clientId;
    private final String clientSecret;

    private String accessToken;
    private Instant tokenExpiry;
    private final ReentrantLock lock = new ReentrantLock();

    public Auth0Service(
            WebClient client,
            @Value("${auth0.issuer}") String issuer,
            @Value("${auth0.client_id}") String clientId,
            @Value("${auth0.client_secret}") String clientSecret
    ) {
        this.client = client;
        this.issuer = issuer;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    private String getMachineToken() {
        lock.lock();
        try {
            if (accessToken != null
                    && tokenExpiry != null
                    && tokenExpiry.isAfter(Instant.now())) {
                return accessToken;
            }

            String tokenEndpoint = issuer + "oauth/token";

            Map<String, Object> response = client.post()
                    .uri(tokenEndpoint)
                    .bodyValue(Map.of(
                            "client_id", clientId,
                            "client_secret", clientSecret,
                            "audience", issuer + "api/v2/",
                            "grant_type", "client_credentials"
                    ))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response == null) {
                throw new IllegalStateException("No se pudo obtener el token de Auth0");
            }

            this.accessToken = (String) response.get("access_token");
            Integer expiresIn = (Integer) response.get("expires_in");
            this.tokenExpiry = Instant.now().plusSeconds(expiresIn.longValue());

            return this.accessToken;
        } finally {
            lock.unlock();
        }
    }

    public List<UserDTO> getAllUsers() {
        String usersEndpoint = issuer + "api/v2/users";
        String token = getMachineToken();

        List<Map<String, Object>> response = client.get()
                .uri(usersEndpoint)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                .block();

        if (response == null) {
            return List.of();
        }

        return response.stream()
                .map(map -> new UserDTO(
                        (String) map.get("user_id"),   // <-- ESTE campo es friendId
                        (String) map.getOrDefault("nickname", "")
                ))
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(String userId) {
        String userEndpoint = issuer + "api/v2/users/" + userId;
        String token = getMachineToken();

        Map<String, Object> response = client.get()
                .uri(userEndpoint)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        if (response == null) {
            throw new IllegalStateException("No se pudo obtener el usuario de Auth0");
        }

        return new UserDTO(
                (String) response.get("user_id"),
                (String) response.getOrDefault("nickname", "")
        );
    }

    public String getUserId(Authentication authentication) {
        System.out.println(authentication);
        Jwt token = (Jwt) authentication.getPrincipal();
        return token.getSubject();
    }
}
