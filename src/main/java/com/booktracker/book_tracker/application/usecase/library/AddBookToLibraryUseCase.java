package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.application.usecase.book.BookMetadata;
import com.booktracker.book_tracker.application.usecase.book.BookResolver;
import com.booktracker.book_tracker.domain.model.Book;
import com.booktracker.book_tracker.domain.model.UserBook;
import com.booktracker.book_tracker.domain.repository.UserBookRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AddBookToLibraryUseCase {

    private final BookResolver bookResolver;
    private final UserBookRepository userBookRepository;

    public AddBookToLibraryUseCase(BookResolver bookResolver, UserBookRepository userBookRepository) {
        this.bookResolver = bookResolver;
        this.userBookRepository = userBookRepository;
    }

    /**
     * Idempotent: ensures the user_books association exists and returns it,
     * whether newly created or already present. Also backs the "Want to Read"
     * shelf action — adding a book to the library and marking it "Want to
     * Read" are the same underlying fact (a user_books row with no reading
     * instances yet), so a repeat call is a no-op, not a conflict.
     *
     * output.created() tells the caller (LibraryController) whether to
     * respond 201 (new association) or 200 (existing association returned) —
     * this use case doesn't know about HTTP, it just reports the fact.
     */
    public AddBookToLibraryOutput execute(AddBookToLibraryInput input) {
        Book book = bookResolver.resolveOrCreate(toBookMetadata(input));

        var existing = userBookRepository.findByUserIdAndBookId(input.userId(), book.getId());
        if (existing.isPresent()) {
            UserBook userBook = existing.get();
            return new AddBookToLibraryOutput(
                    userBook.getId(),
                    book.getId(),
                    userBook.getOwnedFormat(),
                    userBook.getCreatedAt() != null ? userBook.getCreatedAt() : Instant.now(),
                    false
            );
        }

        UserBook newUserBook = new UserBook();
        newUserBook.setUserId(input.userId());
        newUserBook.setBookId(book.getId());
        newUserBook.setOwnedFormat(input.ownedFormat());
        UserBook saved = userBookRepository.save(newUserBook);

        return new AddBookToLibraryOutput(
                saved.getId(),
                book.getId(),
                saved.getOwnedFormat(),
                saved.getCreatedAt() != null ? saved.getCreatedAt() : Instant.now(),
                true
        );
    }

    private BookMetadata toBookMetadata(AddBookToLibraryInput input) {
        return new BookMetadata(
                input.externalSource(),
                input.externalId(),
                input.isbn13(),
                input.isbn10(),
                input.title(),
                input.subtitle(),
                input.authors(),
                input.publisher(),
                input.publishedDate(),
                input.description(),
                input.pageCount(),
                input.genres(),
                input.coverImageUrl(),
                input.language()
        );
    }
}