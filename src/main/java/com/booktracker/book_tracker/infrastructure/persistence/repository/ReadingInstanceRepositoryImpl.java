package com.booktracker.book_tracker.infrastructure.persistence.repository;

import com.booktracker.book_tracker.domain.model.ReadingInstance;
import com.booktracker.book_tracker.domain.repository.ReadingInstanceRepository;
import com.booktracker.book_tracker.domain.valueobject.ReadingStatus;
import com.booktracker.book_tracker.infrastructure.persistence.entity.ReadingInstanceEntity;
import com.booktracker.book_tracker.infrastructure.persistence.mapper.ReadingInstanceEntityMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ReadingInstanceRepositoryImpl implements ReadingInstanceRepository {

    private final JpaReadingInstanceRepository jpaReadingInstanceRepository;
    private final ReadingInstanceEntityMapper mapper;

    public ReadingInstanceRepositoryImpl(JpaReadingInstanceRepository jpaReadingInstanceRepository,
                                          ReadingInstanceEntityMapper mapper) {
        this.jpaReadingInstanceRepository = jpaReadingInstanceRepository;
        this.mapper = mapper;
    }

    @Override
    public ReadingInstance save(ReadingInstance readingInstance) {
        ReadingInstanceEntity entity = mapper.toEntity(readingInstance);
        Instant now = Instant.now();
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        entity.setUpdatedAt(now);
        return mapper.toDomain(jpaReadingInstanceRepository.save(entity));
    }

    @Override
    public Optional<ReadingInstance> findById(UUID id) {
        return jpaReadingInstanceRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Integer> findMaxReadNumberByUserBookId(UUID userBookId) {
        return jpaReadingInstanceRepository.findMaxReadNumberByUserBookId(userBookId);
    }

    @Override
    public boolean existsActiveReadingInstance(UUID userBookId) {
        return jpaReadingInstanceRepository.existsByUserBookIdAndStatus(userBookId, ReadingStatus.READING);
    }
}