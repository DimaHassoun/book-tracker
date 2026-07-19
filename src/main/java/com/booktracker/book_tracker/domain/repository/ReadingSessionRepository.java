package com.booktracker.book_tracker.domain.repository;

import com.booktracker.book_tracker.domain.model.ReadingSession;

/**
 * Persistence contract for {@link ReadingSession}. Only {@code save} is
 * exposed for now — listing/editing/deleting sessions belongs to a later
 * batch once the read-side of the library (Batch 4) is designed.
 */
public interface ReadingSessionRepository {
    ReadingSession save(ReadingSession readingSession);
}