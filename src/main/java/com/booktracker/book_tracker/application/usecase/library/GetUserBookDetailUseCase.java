package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.domain.exception.UserBookNotFoundException;
import com.booktracker.book_tracker.domain.model.Book;
import com.booktracker.book_tracker.domain.model.ReadingInstance;
import com.booktracker.book_tracker.domain.model.UserBook;
import com.booktracker.book_tracker.domain.repository.BookRepository;
import com.booktracker.book_tracker.domain.repository.ReadingInstanceRepository;
import com.booktracker.book_tracker.domain.repository.UserBookRepository;
import com.booktracker.book_tracker.domain.valueobject.Shelf;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Read-only: single book's library entry plus its full reading-instance
 * history (most recent first). This is the endpoint a book-detail page
 * calls to decide client-side whether pressing "Reading" would start a
 * reread — i.e. whether readingInstances is non-empty and its first
 * element's status is terminal (READ/DNF).
 */
@Service
public class GetUserBookDetailUseCase {

    private final UserBookRepository userBookRepository;
    private final ReadingInstanceRepository readingInstanceRepository;
    private final BookRepository bookRepository;

    public GetUserBookDetailUseCase(UserBookRepository userBookRepository,
                                     ReadingInstanceRepository readingInstanceRepository,
                                     BookRepository bookRepository) {
        this.userBookRepository = userBookRepository;
        this.readingInstanceRepository = readingInstanceRepository;
        this.bookRepository = bookRepository;
    }

    public UserBookDetailOutput execute(GetUserBookDetailInput input) {
        UserBook userBook = userBookRepository.findById(input.userBookId())
                .filter(ub -> ub.getUserId().equals(input.userId()))
                .orElseThrow(() -> new UserBookNotFoundException(
                        "No library entry " + input.userBookId() + " found for this user"));

        Book book = bookRepository.findById(userBook.getBookId())
                .orElseThrow(() -> new IllegalStateException(
                        "Book " + userBook.getBookId() + " referenced by user_book " + userBook.getId()
                                + " is missing — data integrity violation"));

        List<ReadingInstance> instances = readingInstanceRepository.findAllByUserBookId(userBook.getId());

        Shelf shelf = Shelf.fromLatestStatus(instances.isEmpty() ? null : instances.get(0).getStatus());

        List<ReadingInstanceSummary> summaries = instances.stream()
                .map(this::toSummary)
                .toList();

        return new UserBookDetailOutput(
                userBook.getId(),
                book.getId(),
                book.getTitle(),
                book.getSubtitle(),
                book.getAuthors(),
                book.getCoverImageUrl(),
                userBook.getOwnedFormat(),
                shelf,
                userBook.getCreatedAt(),
                summaries
        );
    }

    private ReadingInstanceSummary toSummary(ReadingInstance instance) {
        return new ReadingInstanceSummary(
                instance.getId(),
                instance.getReadNumber(),
                instance.getStatus(),
                instance.getCurrentPage(),
                instance.getStartDate(),
                instance.getEndDate(),
                instance.getCreatedAt()
        );
    }
}