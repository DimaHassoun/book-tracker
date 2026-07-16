package com.booktracker.book_tracker.presentation.dto.response;

import java.util.UUID;

public class AuthResponse {

    private UUID userId;
    private String username;
    private String token;

    public AuthResponse(UUID userId, String username, String token) {
        this.userId = userId;
        this.username = username;
        this.token = token;
    }

    public UUID getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getToken() { return token; }
}