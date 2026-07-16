package com.booktracker.book_tracker.presentation.dto.response;

import com.booktracker.book_tracker.domain.valueobject.BookFormat;

import java.time.Instant;
import java.util.UUID;

public record UserBookResponse(
        UUID userBookId,
        UUID bookId,
        BookFormat ownedFormat,
        Instant createdAt
) {}