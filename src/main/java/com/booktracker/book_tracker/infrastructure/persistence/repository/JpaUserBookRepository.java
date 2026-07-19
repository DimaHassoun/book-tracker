package com.booktracker.book_tracker.infrastructure.persistence.repository;

import com.booktracker.book_tracker.infrastructure.persistence.entity.UserBookEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaUserBookRepository extends JpaRepository<UserBookEntity, UUID> {
    Optional<UserBookEntity> findByUserIdAndBookId(UUID userId, UUID bookId);
    List<UserBookEntity> findAllByUserId(UUID userId);
}