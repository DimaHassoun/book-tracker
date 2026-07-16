package com.booktracker.book_tracker.application.usecase.auth;

import com.booktracker.book_tracker.domain.model.User;

public class AuthResult {

    private final User user;
    private final String token;

    public AuthResult(User user, String token) {
        this.user = user;
        this.token = token;
    }

    public User getUser() { return user; }
    public String getToken() { return token; }
}