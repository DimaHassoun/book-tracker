package com.booktracker.book_tracker.domain.repository;

import com.booktracker.book_tracker.domain.model.Book;
import com.booktracker.book_tracker.domain.valueobject.ExternalSource;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Persistence contract for {@link com.booktracker.book_tracker.domain.model.Book}.
 * {@link #findByExternalSourceAndExternalId} is the lookup that backs
 * "has anyone already referenced this book" — the core of
 * {@link com.booktracker.book_tracker.application.usecase.book.BookResolver}'s
 * find-or-create logic, and the reason {@code books} has a unique
 * constraint on that pair.
 */

public interface BookRepository {
    Book save(Book book);
    Optional<Book> findById(UUID id);
    Optional<Book> findByExternalSourceAndExternalId(ExternalSource externalSource, String externalId);
    List<Book> findByIds(Collection<UUID> ids);
}