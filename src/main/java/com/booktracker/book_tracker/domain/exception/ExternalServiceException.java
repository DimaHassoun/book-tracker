package com.booktracker.book_tracker.domain.exception;

/**
 * Thrown when an outbound call to a third-party book-data API (Google
 * Books, and eventually Open Library) fails — whether the provider
 * returned an error response or the request never completed at all.
 * Mapped to HTTP 503, since the failure is the external dependency's, not
 * the caller's.
 */

public class ExternalServiceException extends RuntimeException {
    public ExternalServiceException(String message) {
        super(message);
    }
}