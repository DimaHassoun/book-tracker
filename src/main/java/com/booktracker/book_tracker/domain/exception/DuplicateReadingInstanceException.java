package com.booktracker.book_tracker.domain.exception;

/**
 * Thrown when an insert would violate the
 * {@code UNIQUE(user_book_id, read_number)} constraint on
 * {@code reading_instances}. This is purely a race-condition backstop —
 * {@code read_number} is normally computed correctly as
 * {@code MAX(read_number) + 1} before the insert is attempted, so this
 * should only ever fire on a genuine concurrent double-submit. Mapped to
 * HTTP 409.
 */

public class DuplicateReadingInstanceException extends RuntimeException {
    public DuplicateReadingInstanceException(String message) {
        super(message);
    }
}