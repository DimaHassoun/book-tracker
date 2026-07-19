package com.booktracker.book_tracker.domain.exception;

/**
 * Thrown when a registration attempt conflicts with an existing user
 * (duplicate username or email). Mapped to HTTP 409 by
 * {@code GlobalExceptionHandler}.
 */

public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException(String message) {
        super(message);
    }
}