package com.booktracker.book_tracker.application.usecase.library;

import java.util.UUID;

public record GetUserBookDetailInput(
        UUID userId,
        UUID userBookId
) {}