package com.snippetsearcher.snippet.controller;

import com.snippetsearcher.snippet.dto.UserDTO;
import com.snippetsearcher.snippet.service.Auth0Service;
import com.snippetsearcher.snippet.service.FriendsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendsController {

    private final FriendsService friendsService;
    private final Auth0Service auth0Service;

    public FriendsController(FriendsService friendsService, Auth0Service auth0Service) {
        this.friendsService = friendsService;
        this.auth0Service = auth0Service;
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getFriends(@AuthenticationPrincipal Jwt jwt) {
        try {
            String userId = jwt.getSubject(); // auth0|...
            List<UserDTO> friends = friendsService.getFriends(userId);
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
