package com.booktracker.book_tracker.domain.exception;

public class ActiveReadingInstanceExistsException extends RuntimeException {
    public ActiveReadingInstanceExistsException(String message) {
        super(message);
    }
}