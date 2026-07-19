package com.booktracker.book_tracker.infrastructure.persistence.repository;

import com.booktracker.book_tracker.domain.model.Book;
import com.booktracker.book_tracker.domain.repository.BookRepository;
import com.booktracker.book_tracker.infrastructure.persistence.entity.BookEntity;
import com.booktracker.book_tracker.infrastructure.persistence.mapper.BookEntityMapper;
import org.springframework.stereotype.Repository;
import com.booktracker.book_tracker.domain.valueobject.ExternalSource;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class BookRepositoryImpl implements BookRepository {

    private final JpaBookRepository jpaBookRepository;
    private final BookEntityMapper mapper;

    public BookRepositoryImpl(JpaBookRepository jpaBookRepository, BookEntityMapper mapper) {
        this.jpaBookRepository = jpaBookRepository;
        this.mapper = mapper;
    }

    @Override
    public Book save(Book book) {
        BookEntity entity = mapper.toEntity(book);
        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }
        return mapper.toDomain(jpaBookRepository.save(entity));
    }

    @Override
    public Optional<Book> findById(UUID id) {
        return jpaBookRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Book> findByExternalSourceAndExternalId(ExternalSource externalSource,String externalId) {

    return jpaBookRepository
            .findByExternalSourceAndExternalId(
                externalSource,
                externalId)
            .map(mapper::toDomain);
    }

    @Override
    public List<Book> findByIds(Collection<UUID> ids) {
        return jpaBookRepository.findAllById(ids).stream()
                .map(mapper::toDomain)
                .toList();
    }
}