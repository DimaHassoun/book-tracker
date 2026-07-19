package com.booktracker.book_tracker.domain.exception;

/**
 * Thrown when a requested reading instance doesn't exist, or exists but
 * belongs to a different user (via a user_books entry not owned by the
 * caller). Deliberately doesn't distinguish the two cases in its message,
 * matching UserBookNotFoundException's rationale — a 404 either way avoids
 * confirming to a caller that a given instance id belongs to someone else.
 * Mapped to HTTP 404.
 */
public class ReadingInstanceNotFoundException extends RuntimeException {
    public ReadingInstanceNotFoundException(String message) {
        super(message);
    }
}