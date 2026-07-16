package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.domain.valueobject.ReadingStatus;

import java.time.LocalDate;
import java.util.UUID;

public record StartReadingInstanceInput(
        UUID userId,
        UUID userBookId,
        ReadingStatus status,
        Integer currentPage,   // nullable, user-supplied only
        LocalDate startDate,   // nullable, explicit user override
        LocalDate endDate      // nullable, explicit user override
) {}