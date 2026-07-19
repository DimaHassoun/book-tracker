package com.booktracker.book_tracker.domain.valueobject;

/**
 * The physical form in which a user owns or consumes a book
 * (e.g. for {@link com.booktracker.book_tracker.domain.model.UserBook#getOwnedFormat()}).
 * Purely descriptive — has no effect on reading-instance business rules.
 */

public enum BookFormat {
    PHYSICAL, EBOOK, AUDIOBOOK
}