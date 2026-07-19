package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.domain.valueobject.BookFormat;
import com.booktracker.book_tracker.domain.valueobject.ReadingStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AddBookAndStartReadingOutput(
        UUID userBookId,
        UUID bookId,
        BookFormat ownedFormat,
        UUID readingInstanceId,
        int readNumber,
        ReadingStatus status,
        Integer currentPage,
        LocalDate startDate,
        LocalDate endDate,
        boolean isReread,   // readNumber > 1 — informational, for frontend messaging/analytics
        Instant createdAt
) {}