package com.booktracker.book_tracker.application.usecase.session;

import java.time.LocalDate;
import java.util.UUID;

public record LogReadingSessionInput(
        UUID userId,
        UUID readingInstanceId,
        LocalDate sessionDate,
        Integer startPage,
        Integer endPage,
        Integer durationMinutes
) {}