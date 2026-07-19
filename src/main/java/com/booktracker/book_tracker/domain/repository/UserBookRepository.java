package com.booktracker.book_tracker.domain.repository;

import com.booktracker.book_tracker.domain.model.UserBook;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence contract for {@link com.booktracker.book_tracker.domain.model.UserBook}.
 * {@link #findByUserIdAndBookId} backs the idempotent
 * "add to library"/"want to read" action; {@link #findById} backs looking
 * up a specific library entry (and verifying ownership) when starting or
 * continuing a reading instance.
 */

public interface UserBookRepository {
    UserBook save(UserBook userBook);
    Optional<UserBook> findByUserIdAndBookId(UUID userId, UUID bookId);
    Optional<UserBook> findById(UUID id);
    List<UserBook> findAllByUserId(UUID userId);
}