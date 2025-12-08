package com.snippetsearcher.snippet.service;

import com.snippetsearcher.snippet.dto.UserDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FriendsService {

    private final Auth0Service auth0Service;

    public FriendsService(Auth0Service auth0Service) {
        this.auth0Service = auth0Service;
    }
    public List<UserDTO> getFriends(String userId) {
        return auth0Service.getAllUsers().stream()
                .filter(u -> !u.userId().equals(userId))
                .collect(Collectors.toList());
    }
}
