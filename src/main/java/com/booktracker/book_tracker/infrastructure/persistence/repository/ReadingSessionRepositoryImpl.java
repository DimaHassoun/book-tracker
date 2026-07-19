package com.booktracker.book_tracker.infrastructure.persistence.repository;

import com.booktracker.book_tracker.domain.model.ReadingSession;
import com.booktracker.book_tracker.domain.repository.ReadingSessionRepository;
import com.booktracker.book_tracker.infrastructure.persistence.entity.ReadingSessionEntity;
import com.booktracker.book_tracker.infrastructure.persistence.mapper.ReadingSessionEntityMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public class ReadingSessionRepositoryImpl implements ReadingSessionRepository {

    private final JpaReadingSessionRepository jpaReadingSessionRepository;
    private final ReadingSessionEntityMapper mapper;

    public ReadingSessionRepositoryImpl(JpaReadingSessionRepository jpaReadingSessionRepository,
                                         ReadingSessionEntityMapper mapper) {
        this.jpaReadingSessionRepository = jpaReadingSessionRepository;
        this.mapper = mapper;
    }

    @Override
    public ReadingSession save(ReadingSession readingSession) {
        ReadingSessionEntity entity = mapper.toEntity(readingSession);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }
        return mapper.toDomain(jpaReadingSessionRepository.save(entity));
    }
}