package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.domain.valueobject.BookFormat;

import java.time.Instant;
import java.util.UUID;

public record AddBookToLibraryOutput(
        UUID userBookId,
        UUID bookId,
        BookFormat ownedFormat,
        Instant createdAt,
        boolean created   // true only if this call created a new user_books row
) {}