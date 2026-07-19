package com.booktracker.book_tracker.domain.exception;

/**
 * Thrown when a user tries to move a book's latest reading instance from a
 * terminal state ({@code READ} or {@code DNF}) into an active one
 * ({@code READING} or {@code PAUSED}) without setting
 * {@code confirmReread = true}. This is a deliberate confirmation gate —
 * starting a new read of a previously finished/abandoned book is a real,
 * history-changing decision (it creates a new {@code ReadingInstance} with
 * an incremented {@code readNumber}), not something that should happen
 * implicitly from an ambiguous status update. Mapped to HTTP 409.
 */

public class RereadConfirmationRequiredException extends RuntimeException {
    public RereadConfirmationRequiredException(String message) {
        super(message);
    }
}