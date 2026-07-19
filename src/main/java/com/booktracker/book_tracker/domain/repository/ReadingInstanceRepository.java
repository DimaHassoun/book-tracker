package com.booktracker.book_tracker.domain.repository;

import com.booktracker.book_tracker.domain.model.ReadingInstance;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence contract for {@link com.booktracker.book_tracker.domain.model.ReadingInstance}.
 * {@link #findLatestByUserBookId} is the key lookup for all of
 * {@link com.booktracker.book_tracker.application.usecase.library.StartReadingInstanceUseCase}'s
 * decision logic — resume vs. reread vs. no-op vs. correction all hinge on
 * inspecting the single most recent instance for a given library entry,
 * never the full history.
 */

public interface ReadingInstanceRepository {
    ReadingInstance save(ReadingInstance readingInstance);
    Optional<ReadingInstance> findById(UUID id);
    Optional<ReadingInstance> findLatestByUserBookId(UUID userBookId);

    /** Full history for one book, ordered most-recent-read first — backs the book detail view. */
    List<ReadingInstance> findAllByUserBookId(UUID userBookId);

    /**
     * Batch version of findLatestByUserBookId — one query for a whole
     * library list, instead of one query per book (N+1). Absent keys mean
     * that user_book has no reading instances at all (WANT_TO_READ).
     */
    Map<UUID, ReadingInstance> findLatestByUserBookIds(Collection<UUID> userBookIds);
}