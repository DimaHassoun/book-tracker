package com.booktracker.book_tracker.domain.exception;

/**
 * Thrown when an action would create a second concurrently-active
 * {@code READING} instance for the same {@code UserBook}. Only one active
 * read is allowed at a time per book; resuming a {@code PAUSED} instance
 * or logging progress on the existing one are the correct actions instead
 * of starting a new one. Mapped to HTTP 409.
 */

public class ActiveReadingInstanceExistsException extends RuntimeException {
    public ActiveReadingInstanceExistsException(String message) {
        super(message);
    }
}