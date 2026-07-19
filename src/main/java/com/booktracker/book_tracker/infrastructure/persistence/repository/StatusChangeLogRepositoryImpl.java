package com.booktracker.book_tracker.infrastructure.persistence.repository;

import com.booktracker.book_tracker.domain.model.StatusChangeLog;
import com.booktracker.book_tracker.domain.repository.StatusChangeLogRepository;
import com.booktracker.book_tracker.infrastructure.persistence.entity.StatusChangeLogEntity;
import com.booktracker.book_tracker.infrastructure.persistence.mapper.StatusChangeLogEntityMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public class StatusChangeLogRepositoryImpl implements StatusChangeLogRepository {

    private final JpaStatusChangeLogRepository jpaStatusChangeLogRepository;
    private final StatusChangeLogEntityMapper mapper;

    public StatusChangeLogRepositoryImpl(JpaStatusChangeLogRepository jpaStatusChangeLogRepository,
                                          StatusChangeLogEntityMapper mapper) {
        this.jpaStatusChangeLogRepository = jpaStatusChangeLogRepository;
        this.mapper = mapper;
    }

    @Override
    public StatusChangeLog save(StatusChangeLog statusChangeLog) {
        StatusChangeLogEntity entity = mapper.toEntity(statusChangeLog);
        if (entity.getChangedAt() == null) {
            entity.setChangedAt(Instant.now());
        }
        return mapper.toDomain(jpaStatusChangeLogRepository.save(entity));
    }
}