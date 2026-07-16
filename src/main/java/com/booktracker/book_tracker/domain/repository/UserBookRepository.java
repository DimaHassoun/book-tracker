package com.booktracker.book_tracker.domain.repository;

import com.booktracker.book_tracker.domain.model.UserBook;

import java.util.Optional;
import java.util.UUID;

public interface UserBookRepository {
    UserBook save(UserBook userBook);
    Optional<UserBook> findByUserIdAndBookId(UUID userId, UUID bookId);
	Optional<UserBook> findById(UUID id); // Added at Step 5: Reading instances + sessions
}