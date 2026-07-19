package com.booktracker.book_tracker.presentation.dto.response;

import com.booktracker.book_tracker.domain.valueobject.ReadingStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReadingInstanceSummaryResponse(
        UUID id,
        int readNumber,
        ReadingStatus status,
        Integer currentPage,
        LocalDate startDate,
        LocalDate endDate,
        Instant createdAt
) {}