package com.booktracker.book_tracker.infrastructure.persistence.repository;

import com.booktracker.book_tracker.infrastructure.persistence.entity.ReadingInstanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaReadingInstanceRepository extends JpaRepository<ReadingInstanceEntity, UUID> {

    Optional<ReadingInstanceEntity> findFirstByUserBookIdOrderByReadNumberDesc(UUID userBookId);

    List<ReadingInstanceEntity> findAllByUserBookIdOrderByReadNumberDesc(UUID userBookId);

    List<ReadingInstanceEntity> findAllByUserBookIdIn(Collection<UUID> userBookIds);
}