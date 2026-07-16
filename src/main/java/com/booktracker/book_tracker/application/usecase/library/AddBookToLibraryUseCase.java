package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.domain.exception.DuplicateBookException;
import com.booktracker.book_tracker.domain.model.Book;
import com.booktracker.book_tracker.domain.model.UserBook;
import com.booktracker.book_tracker.domain.repository.BookRepository;
import com.booktracker.book_tracker.domain.repository.UserBookRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AddBookToLibraryUseCase {

    private final BookRepository bookRepository;
    private final UserBookRepository userBookRepository;

    public AddBookToLibraryUseCase(BookRepository bookRepository,
                                    UserBookRepository userBookRepository) {
        this.bookRepository = bookRepository;
        this.userBookRepository = userBookRepository;
    }

    public AddBookToLibraryOutput execute(AddBookToLibraryInput input) {
        Book book = bookRepository
                .findByExternalSourceAndExternalId(input.externalSource(), input.externalId())
                .orElseGet(() -> bookRepository.save(toNewBook(input)));

        userBookRepository.findByUserIdAndBookId(input.userId(), book.getId())
                .ifPresent(existing -> {
                    throw new DuplicateBookException(
                            "Book " + book.getId() + " is already in the user's library");
                });

        UserBook newUserBook = new UserBook();
        newUserBook.setUserId(input.userId());
        newUserBook.setBookId(book.getId());
        newUserBook.setOwnedFormat(input.ownedFormat());

        UserBook saved = userBookRepository.save(newUserBook);

        return new AddBookToLibraryOutput(
                saved.getId(),
                book.getId(),
                saved.getOwnedFormat(),
                saved.getCreatedAt() != null ? saved.getCreatedAt() : Instant.now()
        );
    }

    private Book toNewBook(AddBookToLibraryInput input) {
        Book book = new Book();
        book.setIsbn13(input.isbn13());
        book.setIsbn10(input.isbn10());
        book.setTitle(input.title());
        book.setSubtitle(input.subtitle());
        book.setAuthors(input.authors());
        book.setPublisher(input.publisher());
        book.setPublishedDate(input.publishedDate());
        book.setDescription(input.description());
        book.setPageCount(input.pageCount());
        book.setGenres(input.genres());
        book.setCoverImageUrl(input.coverImageUrl());
        book.setExternalSource(input.externalSource());
        book.setExternalId(input.externalId());
        book.setLanguage(input.language());
        return book;
    }
}