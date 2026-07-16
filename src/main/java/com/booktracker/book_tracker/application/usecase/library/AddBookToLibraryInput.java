package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.domain.valueobject.BookFormat;
import com.booktracker.book_tracker.domain.valueobject.ExternalSource;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AddBookToLibraryInput(
        UUID userId,
        ExternalSource externalSource,
        String externalId,
        String isbn13,
        String isbn10,
        String title,
        String subtitle,
        List<String> authors,
        String publisher,
        LocalDate publishedDate,
        String description,
        Integer pageCount,
        List<String> genres,
        String coverImageUrl,
        String language,
        BookFormat ownedFormat
) {}