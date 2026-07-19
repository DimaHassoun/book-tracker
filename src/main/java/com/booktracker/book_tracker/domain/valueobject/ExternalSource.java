package com.booktracker.book_tracker.domain.valueobject;

/**
 * Identifies which external catalog a {@link com.booktracker.book_tracker.domain.model.Book}
 * was sourced from. Combined with {@code externalId}, this pair is the
 * database's enforcement mechanism for "every book must come from an API,
 * never user-typed metadata" — see the {@code UNIQUE(external_source, external_id)}
 * constraint on the {@code books} table.
 */

public enum ExternalSource {
    GOOGLE_BOOKS,
    OPEN_LIBRARY
}