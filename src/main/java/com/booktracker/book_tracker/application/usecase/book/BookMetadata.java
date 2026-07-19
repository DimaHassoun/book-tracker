package com.booktracker.book_tracker.application.usecase.book;

import com.booktracker.book_tracker.domain.valueobject.ExternalSource;

import java.time.LocalDate;
import java.util.List;

public record BookMetadata(
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
        String language
) {}