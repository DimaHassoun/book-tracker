package com.booktracker.book_tracker.application.usecase.book;

import com.booktracker.book_tracker.domain.model.Book;
import com.booktracker.book_tracker.domain.repository.BookRepository;
import org.springframework.stereotype.Component;

/**
 * Finds the canonical book by (externalSource, externalId), creating it if
 * this is the first time anyone has referenced it. Shared by any use case
 * that needs "this book must exist in the catalog" without caring whether
 * it's new — book creation is idempotent per external id.
 */
@Component
public class BookResolver {

    private final BookRepository bookRepository;

    public BookResolver(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public Book resolveOrCreate(BookMetadata metadata) {
        return bookRepository
                .findByExternalSourceAndExternalId(metadata.externalSource(), metadata.externalId())
                .orElseGet(() -> bookRepository.save(toNewBook(metadata)));
    }

    private Book toNewBook(BookMetadata m) {
        Book book = new Book();
        book.setIsbn13(m.isbn13());
        book.setIsbn10(m.isbn10());
        book.setTitle(m.title());
        book.setSubtitle(m.subtitle());
        book.setAuthors(m.authors());
        book.setPublisher(m.publisher());
        book.setPublishedDate(m.publishedDate());
        book.setDescription(m.description());
        book.setPageCount(m.pageCount());
        book.setGenres(m.genres());
        book.setCoverImageUrl(m.coverImageUrl());
        book.setExternalSource(m.externalSource());
        book.setExternalId(m.externalId());
        book.setLanguage(m.language());
        return book;
    }
}