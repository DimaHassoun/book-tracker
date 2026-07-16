package com.booktracker.book_tracker.infrastructure.persistence.repository;

import com.booktracker.book_tracker.domain.exception.DuplicateBookException;
import com.booktracker.book_tracker.domain.model.UserBook;
import com.booktracker.book_tracker.domain.repository.UserBookRepository;
import com.booktracker.book_tracker.infrastructure.persistence.entity.UserBookEntity;
import com.booktracker.book_tracker.infrastructure.persistence.mapper.UserBookEntityMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserBookRepositoryImpl implements UserBookRepository {

    private final JpaUserBookRepository jpaUserBookRepository;
    private final UserBookEntityMapper mapper;

    public UserBookRepositoryImpl(JpaUserBookRepository jpaUserBookRepository,
                                   UserBookEntityMapper mapper) {
        this.jpaUserBookRepository = jpaUserBookRepository;
        this.mapper = mapper;
    }

    @Override
    public UserBook save(UserBook userBook) {
        UserBookEntity entity = mapper.toEntity(userBook);
        Instant now = Instant.now();
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(now);
        }
        entity.setUpdatedAt(now);

        try {
            UserBookEntity saved = jpaUserBookRepository.save(entity);
            return mapper.toDomain(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateBookException(
                    "Book " + userBook.getBookId() + " is already in the user's library");
        }
    }

    @Override
    public Optional<UserBook> findByUserIdAndBookId(UUID userId, UUID bookId) {
        return jpaUserBookRepository.findByUserIdAndBookId(userId, bookId)
                .map(mapper::toDomain);
    }
	// Added at Step 5: Reading instances + sessions
	@Override
	public Optional<UserBook> findById(UUID id) {
 	   return jpaUserBookRepository.findById(id).map(mapper::toDomain);
	}
}