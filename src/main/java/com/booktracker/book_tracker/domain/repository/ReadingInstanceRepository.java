package com.booktracker.book_tracker.domain.repository;

import com.booktracker.book_tracker.domain.model.ReadingInstance;

import java.util.Optional;
import java.util.UUID;

public interface ReadingInstanceRepository {
    ReadingInstance save(ReadingInstance readingInstance);
    Optional<ReadingInstance> findById(UUID id);
    Optional<Integer> findMaxReadNumberByUserBookId(UUID userBookId);
    boolean existsActiveReadingInstance(UUID userBookId); // status = READING
}