package com.booktracker.book_tracker.infrastructure.persistence.repository;

import com.booktracker.book_tracker.domain.exception.DuplicateReadingInstanceException;
import com.booktracker.book_tracker.domain.model.ReadingInstance;
import com.booktracker.book_tracker.domain.repository.ReadingInstanceRepository;
import com.booktracker.book_tracker.infrastructure.persistence.entity.ReadingInstanceEntity;
import com.booktracker.book_tracker.infrastructure.persistence.mapper.ReadingInstanceEntityMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        try {
            return mapper.toDomain(jpaReadingInstanceRepository.save(entity));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateReadingInstanceException(
                    "Read number " + readingInstance.getReadNumber()
                            + " already exists for user_book " + readingInstance.getUserBookId());
        }
    }

    @Override
    public Optional<ReadingInstance> findById(UUID id) {
        return jpaReadingInstanceRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<ReadingInstance> findLatestByUserBookId(UUID userBookId) {
        return jpaReadingInstanceRepository
                .findFirstByUserBookIdOrderByReadNumberDesc(userBookId)
                .map(mapper::toDomain);
    }

    @Override
    public List<ReadingInstance> findAllByUserBookId(UUID userBookId) {
        return jpaReadingInstanceRepository.findAllByUserBookIdOrderByReadNumberDesc(userBookId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Map<UUID, ReadingInstance> findLatestByUserBookIds(Collection<UUID> userBookIds) {
        if (userBookIds.isEmpty()) {
            return Map.of();
        }

        // Single query for every instance across the whole set of books, then
        // reduce to "highest read_number per user_book_id" in memory — avoids
        // one query per book (N+1) for what's typically a small result set
        // (one user's library).
        Map<UUID, ReadingInstanceEntity> latestByUserBookId = new HashMap<>();
        for (ReadingInstanceEntity entity : jpaReadingInstanceRepository.findAllByUserBookIdIn(userBookIds)) {
            latestByUserBookId.merge(entity.getUserBookId(), entity,
                    (current, candidate) -> candidate.getReadNumber() > current.getReadNumber() ? candidate : current);
        }

        Map<UUID, ReadingInstance> result = new HashMap<>();
        latestByUserBookId.forEach((userBookId, entity) -> result.put(userBookId, mapper.toDomain(entity)));
        return result;
    }
}