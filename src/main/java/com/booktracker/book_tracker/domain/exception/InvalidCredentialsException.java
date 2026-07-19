package com.booktracker.book_tracker.domain.exception;

/**
 * Thrown on a failed login attempt — unknown identifier or wrong password.
 * Deliberately does not distinguish which one failed, to avoid leaking
 * whether a given username/email is registered. Mapped to HTTP 401.
 */

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}