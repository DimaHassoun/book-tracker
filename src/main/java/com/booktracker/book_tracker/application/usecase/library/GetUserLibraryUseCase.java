package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.domain.model.Book;
import com.booktracker.book_tracker.domain.model.ReadingInstance;
import com.booktracker.book_tracker.domain.model.UserBook;
import com.booktracker.book_tracker.domain.repository.BookRepository;
import com.booktracker.book_tracker.domain.repository.ReadingInstanceRepository;
import com.booktracker.book_tracker.domain.repository.UserBookRepository;
import com.booktracker.book_tracker.domain.valueobject.Shelf;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Read-only: lists a user's library, each entry annotated with its derived
 * shelf. Nothing here is stored shelf state — Shelf.fromLatestStatus is
 * computed per request from user_books + the latest reading_instance per
 * book, fetched in two batch queries (never one query per book).
 */
@Service
public class GetUserLibraryUseCase {

    private final UserBookRepository userBookRepository;
    private final ReadingInstanceRepository readingInstanceRepository;
    private final BookRepository bookRepository;

    public GetUserLibraryUseCase(UserBookRepository userBookRepository,
                                  ReadingInstanceRepository readingInstanceRepository,
                                  BookRepository bookRepository) {
        this.userBookRepository = userBookRepository;
        this.readingInstanceRepository = readingInstanceRepository;
        this.bookRepository = bookRepository;
    }

    public List<LibraryItemOutput> execute(GetUserLibraryInput input) {
        List<UserBook> userBooks = userBookRepository.findAllByUserId(input.userId());
        if (userBooks.isEmpty()) {
            return List.of();
        }

        List<UUID> userBookIds = userBooks.stream().map(UserBook::getId).toList();
        List<UUID> bookIds = userBooks.stream().map(UserBook::getBookId).toList();

        Map<UUID, ReadingInstance> latestByUserBookId = readingInstanceRepository.findLatestByUserBookIds(userBookIds);
        Map<UUID, Book> booksById = bookRepository.findByIds(bookIds).stream()
                .collect(java.util.stream.Collectors.toMap(Book::getId, b -> b));

        return userBooks.stream()
                .map(userBook -> toItem(userBook, booksById.get(userBook.getBookId()),
                        latestByUserBookId.get(userBook.getId())))
                .filter(item -> item != null)
                .filter(item -> input.shelfFilter() == null || item.shelf() == input.shelfFilter())
                .toList();
    }

    private LibraryItemOutput toItem(UserBook userBook, Book book, ReadingInstance latestInstance) {
        if (book == null) {
            // Shouldn't happen — books.id is referenced by a NOT NULL, RESTRICT FK
            // from user_books. Skip defensively rather than fail the whole list.
            return null;
        }

        Shelf shelf = Shelf.fromLatestStatus(latestInstance != null ? latestInstance.getStatus() : null);

        return new LibraryItemOutput(
                userBook.getId(),
                book.getId(),
                book.getTitle(),
                book.getSubtitle(),
                book.getAuthors(),
                book.getCoverImageUrl(),
                userBook.getOwnedFormat(),
                shelf,
                userBook.getCreatedAt()
        );
    }
}