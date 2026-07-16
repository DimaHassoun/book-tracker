package com.booktracker.book_tracker.domain.repository;

import com.booktracker.book_tracker.domain.model.Book;
import com.booktracker.book_tracker.domain.valueobject.ExternalSource;
import java.util.Optional;
import java.util.UUID;

public interface BookRepository {
    Book save(Book book);
    Optional<Book> findById(UUID id);
    Optional<Book> findByExternalSourceAndExternalId(ExternalSource externalSource, String externalId);
}