package com.booktracker.book_tracker.domain.exception;

public class UserBookNotFoundException extends RuntimeException {
    public UserBookNotFoundException(String message) {
        super(message);
    }
}