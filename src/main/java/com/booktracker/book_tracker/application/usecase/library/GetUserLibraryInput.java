package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.domain.valueobject.Shelf;

import java.util.UUID;

public record GetUserLibraryInput(
        UUID userId,
        Shelf shelfFilter   // nullable — no filter means "all shelves"
) {}