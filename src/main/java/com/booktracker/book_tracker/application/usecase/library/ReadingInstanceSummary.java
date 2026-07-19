package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.domain.valueobject.ReadingStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReadingInstanceSummary(
        UUID id,
        int readNumber,
        ReadingStatus status,
        Integer currentPage,
        LocalDate startDate,
        LocalDate endDate,
        Instant createdAt
) {}