package com.booktracker.book_tracker.domain.valueobject;

/**
 * A read-model concept only — never persisted. Every shelf is derived from
 * existing {@code user_books} / {@code reading_instances} data at query
 * time: WANT_TO_READ means a user_books row exists with no reading
 * instances; the other four mirror the latest reading instance's status.
 * There is deliberately no WANT_TO_READ value in {@link ReadingStatus} and
 * no stored shelf column anywhere — this enum exists purely so read-side
 * use cases have a single, shared place to compute "which shelf" instead
 * of duplicating the same if/else in multiple places.
 */
public enum Shelf {
    WANT_TO_READ, READING, PAUSED, READ, DNF;

    public static Shelf fromLatestStatus(ReadingStatus latestStatus) {
        if (latestStatus == null) {
            return WANT_TO_READ;
        }
        return switch (latestStatus) {
            case READING -> READING;
            case PAUSED -> PAUSED;
            case READ -> READ;
            case DNF -> DNF;
        };
    }
}