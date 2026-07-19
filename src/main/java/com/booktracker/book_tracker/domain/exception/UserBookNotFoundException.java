package com.booktracker.book_tracker.domain.exception;

/**
 * Thrown when a requested library entry doesn't exist, or exists but
 * belongs to a different user. Deliberately doesn't distinguish the two
 * cases in its message — a 404 either way avoids confirming to a caller
 * that a given library-entry id belongs to someone else. Mapped to HTTP 404.
 */

public class UserBookNotFoundException extends RuntimeException {
    public UserBookNotFoundException(String message) {
        super(message);
    }
}