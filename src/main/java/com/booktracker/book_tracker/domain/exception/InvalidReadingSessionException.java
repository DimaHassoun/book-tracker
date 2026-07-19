package com.booktracker.book_tracker.domain.exception;

/**
 * Thrown when a reading session fails business validation: a non-positive
 * page range (end_page <= start_page), a negative start_page, a future
 * session_date, or a non-positive duration when one is supplied. Mapped to
 * HTTP 400 — these are all caller input errors, not conflicts or missing
 * resources.
 */
public class InvalidReadingSessionException extends RuntimeException {
    public InvalidReadingSessionException(String message) {
        super(message);
    }
}