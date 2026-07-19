package com.booktracker.book_tracker.domain.repository;

import com.booktracker.book_tracker.domain.model.StatusChangeLog;

/**
 * Persistence contract for {@link com.booktracker.book_tracker.domain.model.StatusChangeLog}.
 * Append-only by convention — only {@code save} is exposed; there is
 * deliberately no update or delete, since a status-change log entry
 * represents history that already happened and should never be edited.
 */

public interface StatusChangeLogRepository {
    StatusChangeLog save(StatusChangeLog statusChangeLog);
}