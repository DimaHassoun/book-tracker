package com.booktracker.book_tracker.infrastructure.persistence.mapper;

import com.booktracker.book_tracker.domain.model.Book;
import com.booktracker.book_tracker.infrastructure.persistence.entity.BookEntity;
import org.springframework.stereotype.Component;

@Component
public class BookEntityMapper {

    public Book toDomain(BookEntity entity) {
        if (entity == null) return null;
        Book book = new Book();
        book.setId(entity.getId());
        book.setIsbn13(entity.getIsbn13());
        book.setIsbn10(entity.getIsbn10());
        book.setTitle(entity.getTitle());
        book.setSubtitle(entity.getSubtitle());
        book.setAuthors(entity.getAuthors());
        book.setPublisher(entity.getPublisher());
        book.setPublishedDate(entity.getPublishedDate());
        book.setDescription(entity.getDescription());
        book.setPageCount(entity.getPageCount());
        book.setGenres(entity.getGenres());
        book.setCoverImageUrl(entity.getCoverImageUrl());
        book.setExternalSource(entity.getExternalSource());
        book.setExternalId(entity.getExternalId());
        book.setLanguage(entity.getLanguage());
        book.setCreatedAt(entity.getCreatedAt());
        return book;
    }

    public BookEntity toEntity(Book book) {
        if (book == null) return null;
        BookEntity entity = new BookEntity();
        entity.setId(book.getId());
        entity.setIsbn13(book.getIsbn13());
        entity.setIsbn10(book.getIsbn10());
        entity.setTitle(book.getTitle());
        entity.setSubtitle(book.getSubtitle());
        entity.setAuthors(book.getAuthors());
        entity.setPublisher(book.getPublisher());
        entity.setPublishedDate(book.getPublishedDate());
        entity.setDescription(book.getDescription());
        entity.setPageCount(book.getPageCount());
        entity.setGenres(book.getGenres());
        entity.setCoverImageUrl(book.getCoverImageUrl());
        entity.setExternalSource(book.getExternalSource());
        entity.setExternalId(book.getExternalId());
        entity.setLanguage(book.getLanguage());
        entity.setCreatedAt(book.getCreatedAt());
        return entity;
    }
}