package com.booktracker.book_tracker.presentation.dto.request;

import com.booktracker.book_tracker.domain.valueobject.ReadingStatus;

import java.time.LocalDate;

public record StartReadingInstanceRequest(
        ReadingStatus status,
        Integer currentPage,
        LocalDate startDate,
        LocalDate endDate,
        Boolean confirmReread
) {}