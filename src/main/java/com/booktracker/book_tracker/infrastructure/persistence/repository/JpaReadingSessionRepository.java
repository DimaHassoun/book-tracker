package com.booktracker.book_tracker.infrastructure.persistence.repository;

import com.booktracker.book_tracker.infrastructure.persistence.entity.ReadingSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaReadingSessionRepository extends JpaRepository<ReadingSessionEntity, UUID> {
}