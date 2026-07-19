package com.booktracker.book_tracker.domain.valueobject;

/**
 * The lifecycle state of a single {@link com.booktracker.book_tracker.domain.model.ReadingInstance}.
 * 
 * Deliberately does NOT include a "want to read" state — that concept is
 * represented by the mere existence of a {@code user_books} row with no
 * reading instances yet, not by a status value here. A reading instance is
 * only ever created once the user takes an actual reading-related action.
 * 
 * {@link #READ} and {@link #DNF} are terminal states: moving from either of
 * them into {@link #READING} or {@link #PAUSED} represents starting a new
 * read (a reread), not continuing the old one, and requires explicit
 * confirmation at the use-case layer.
 */

public enum ReadingStatus {
    READING, PAUSED, DNF, READ
}