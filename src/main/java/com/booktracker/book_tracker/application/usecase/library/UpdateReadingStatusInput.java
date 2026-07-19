package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.domain.valueobject.ReadingStatus;

import java.time.LocalDate;
import java.util.UUID;

public record UpdateReadingStatusInput(
        UUID userId,
        UUID readingInstanceId,
        ReadingStatus status,
        Integer currentPage,
        LocalDate startDate,
        LocalDate endDate
) {}