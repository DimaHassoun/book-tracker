package com.booktracker.book_tracker.infrastructure.persistence.repository;

import com.booktracker.book_tracker.domain.valueobject.ReadingStatus;
import com.booktracker.book_tracker.infrastructure.persistence.entity.ReadingInstanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaReadingInstanceRepository extends JpaRepository<ReadingInstanceEntity, UUID> {

    @Query("SELECT MAX(r.readNumber) FROM ReadingInstanceEntity r WHERE r.userBookId = :userBookId")
    Optional<Integer> findMaxReadNumberByUserBookId(@Param("userBookId") UUID userBookId);

    boolean existsByUserBookIdAndStatus(UUID userBookId, ReadingStatus status);
}