package com.booktracker.book_tracker.infrastructure.persistence.repository;
import java.util.List;

import com.booktracker.book_tracker.domain.model.UserBook;
import com.booktracker.book_tracker.domain.repository.UserBookRepository;
import com.booktracker.book_tracker.infrastructure.persistence.entity.UserBookEntity;
import com.booktracker.book_tracker.infrastructure.persistence.mapper.UserBookEntityMapper;

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

    	UserBookEntity saved = jpaUserBookRepository.save(entity);
    	return mapper.toDomain(saved);

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
	
	@Override
	public List<UserBook> findAllByUserId(UUID userId) {
		return jpaUserBookRepository.findAllByUserId(userId).stream()
				.map(mapper::toDomain)
				.toList();
	}
}