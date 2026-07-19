package com.booktracker.book_tracker.application.usecase.session;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record LogReadingSessionOutput(
        UUID id,
        UUID readingInstanceId,
        LocalDate sessionDate,
        int startPage,
        int endPage,
        Integer durationMinutes,
        int pagesRead,
        Instant createdAt
) {}