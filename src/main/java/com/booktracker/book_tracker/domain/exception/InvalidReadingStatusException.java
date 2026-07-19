package com.booktracker.book_tracker.domain.exception;

/**
 * Thrown when a reading-instance action is attempted with no status at
 * all. In practice this should never happen through the intended frontend
 * flow (the user always picks an explicit action), so this exists purely
 * as a defensive backend guard against a malformed request. Mapped to
 * HTTP 400.
 */

public class InvalidReadingStatusException extends RuntimeException {
    public InvalidReadingStatusException(String message) {
        super(message);
    }
}