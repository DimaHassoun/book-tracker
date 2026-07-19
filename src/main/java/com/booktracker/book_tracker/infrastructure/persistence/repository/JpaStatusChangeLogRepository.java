package com.booktracker.book_tracker.infrastructure.persistence.repository;

import com.booktracker.book_tracker.infrastructure.persistence.entity.StatusChangeLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaStatusChangeLogRepository extends JpaRepository<StatusChangeLogEntity, UUID> {
}