package com.booktracker.book_tracker.presentation.dto.request;

import java.time.LocalDate;

public record LogReadingSessionRequest(
        LocalDate sessionDate,
        Integer startPage,
        Integer endPage,
        Integer durationMinutes
) {}