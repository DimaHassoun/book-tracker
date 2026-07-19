package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.domain.exception.UserBookNotFoundException;
import com.booktracker.book_tracker.domain.model.Book;
import com.booktracker.book_tracker.domain.model.ReadingInstance;
import com.booktracker.book_tracker.domain.model.UserBook;
import com.booktracker.book_tracker.domain.repository.BookRepository;
import com.booktracker.book_tracker.domain.repository.ReadingInstanceRepository;
import com.booktracker.book_tracker.domain.repository.UserBookRepository;
import com.booktracker.book_tracker.domain.valueobject.ReadingStatus;
import com.booktracker.book_tracker.domain.valueobject.Shelf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserBookDetailUseCaseTest {

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private ReadingInstanceRepository readingInstanceRepository;

    @Mock
    private BookRepository bookRepository;

    private GetUserBookDetailUseCase useCase;

    private UUID userId;
    private UUID userBookId;
    private UUID bookId;
    private UserBook userBook;

    @BeforeEach
    void setUp() {
        useCase = new GetUserBookDetailUseCase(userBookRepository, readingInstanceRepository, bookRepository);
        userId = UUID.randomUUID();
        userBookId = UUID.randomUUID();
        bookId = UUID.randomUUID();

        userBook = new UserBook();
        userBook.setId(userBookId);
        userBook.setUserId(userId);
        userBook.setBookId(bookId);
        userBook.setCreatedAt(Instant.now());
    }

    @Test
    void execute_whenUserBookNotFound_shouldThrowUserBookNotFoundException() {
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.empty());

        assertThrows(UserBookNotFoundException.class,
                () -> useCase.execute(new GetUserBookDetailInput(userId, userBookId)));
    }

    @Test
    void execute_whenUserBookBelongsToDifferentUser_shouldThrowUserBookNotFoundException() {
        userBook.setUserId(UUID.randomUUID());
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.of(userBook));

        assertThrows(UserBookNotFoundException.class,
                () -> useCase.execute(new GetUserBookDetailInput(userId, userBookId)));
    }

    @Test
    void execute_whenReferencedBookIsMissing_shouldThrowIllegalStateException() {
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.of(userBook));
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> useCase.execute(new GetUserBookDetailInput(userId, userBookId)));
    }

    @Test
    void execute_whenNoReadingInstances_shouldReturnWantToReadShelfAndEmptyHistory() {
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.of(userBook));
        Book book = new Book();
        book.setId(bookId);
        book.setTitle("Dune");
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(readingInstanceRepository.findAllByUserBookId(userBookId)).thenReturn(List.of());

        UserBookDetailOutput output = useCase.execute(new GetUserBookDetailInput(userId, userBookId));

        assertThat(output.shelf()).isEqualTo(Shelf.WANT_TO_READ);
        assertThat(output.readingInstances()).isEmpty();
        assertThat(output.title()).isEqualTo("Dune");
    }

    @Test
    void execute_shouldReturnShelfFromMostRecentInstance_andFullHistoryOrdered() {
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.of(userBook));
        Book book = new Book();
        book.setId(bookId);
        book.setTitle("Dune");
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        ReadingInstance reread = new ReadingInstance();
        reread.setId(UUID.randomUUID());
        reread.setReadNumber(2);
        reread.setStatus(ReadingStatus.READING);

        ReadingInstance firstRead = new ReadingInstance();
        firstRead.setId(UUID.randomUUID());
        firstRead.setReadNumber(1);
        firstRead.setStatus(ReadingStatus.READ);

        // Repository contract: most-recent-read first.
        when(readingInstanceRepository.findAllByUserBookId(userBookId)).thenReturn(List.of(reread, firstRead));

        UserBookDetailOutput output = useCase.execute(new GetUserBookDetailInput(userId, userBookId));

        assertThat(output.shelf()).isEqualTo(Shelf.READING);
        assertThat(output.readingInstances()).hasSize(2);
        assertThat(output.readingInstances().get(0).readNumber()).isEqualTo(2);
        assertThat(output.readingInstances().get(1).readNumber()).isEqualTo(1);
    }
}
