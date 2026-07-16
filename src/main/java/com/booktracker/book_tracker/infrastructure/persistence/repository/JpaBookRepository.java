package com.booktracker.book_tracker.infrastructure.persistence.repository;

import com.booktracker.book_tracker.infrastructure.persistence.entity.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import com.booktracker.book_tracker.domain.valueobject.ExternalSource;
import java.util.Optional;
import java.util.UUID;

public interface JpaBookRepository extends JpaRepository<BookEntity, UUID> {
    Optional<BookEntity> findByExternalSourceAndExternalId(ExternalSource source, String externalId);
}