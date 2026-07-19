package com.booktracker.book_tracker.presentation.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReadingSessionResponse(
        UUID id,
        UUID readingInstanceId,
        LocalDate sessionDate,
        int startPage,
        int endPage,
        Integer durationMinutes,
        int pagesRead,
        Instant createdAt
) {}