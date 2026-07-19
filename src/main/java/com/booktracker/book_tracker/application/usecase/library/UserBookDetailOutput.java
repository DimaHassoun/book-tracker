package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.domain.valueobject.BookFormat;
import com.booktracker.book_tracker.domain.valueobject.Shelf;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserBookDetailOutput(
        UUID userBookId,
        UUID bookId,
        String title,
        String subtitle,
        List<String> authors,
        String coverImageUrl,
        BookFormat ownedFormat,
        Shelf shelf,
        Instant addedAt,
        List<ReadingInstanceSummary> readingInstances   // most-recent read first; empty means WANT_TO_READ
) {}