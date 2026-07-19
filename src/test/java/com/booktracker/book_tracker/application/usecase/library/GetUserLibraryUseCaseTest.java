package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.domain.model.Book;
import com.booktracker.book_tracker.domain.model.ReadingInstance;
import com.booktracker.book_tracker.domain.model.UserBook;
import com.booktracker.book_tracker.domain.repository.BookRepository;
import com.booktracker.book_tracker.domain.repository.ReadingInstanceRepository;
import com.booktracker.book_tracker.domain.repository.UserBookRepository;
import com.booktracker.book_tracker.domain.valueobject.BookFormat;
import com.booktracker.book_tracker.domain.valueobject.ReadingStatus;
import com.booktracker.book_tracker.domain.valueobject.Shelf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserLibraryUseCaseTest {

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private ReadingInstanceRepository readingInstanceRepository;

    @Mock
    private BookRepository bookRepository;

    private GetUserLibraryUseCase useCase;

    private UUID userId;

    @BeforeEach
    void setUp() {
        useCase = new GetUserLibraryUseCase(userBookRepository, readingInstanceRepository, bookRepository);
        userId = UUID.randomUUID();
    }

    private UserBook userBook(UUID bookId) {
        UserBook ub = new UserBook();
        ub.setId(UUID.randomUUID());
        ub.setUserId(userId);
        ub.setBookId(bookId);
        ub.setOwnedFormat(BookFormat.PHYSICAL);
        ub.setCreatedAt(Instant.now());
        return ub;
    }

    private Book book(UUID id, String title) {
        Book b = new Book();
        b.setId(id);
        b.setTitle(title);
        return b;
    }

    @Test
    void execute_whenLibraryEmpty_shouldReturnEmptyListWithoutFurtherQueries() {
        when(userBookRepository.findAllByUserId(userId)).thenReturn(List.of());

        List<LibraryItemOutput> result = useCase.execute(new GetUserLibraryInput(userId, null));

        assertThat(result).isEmpty();
        verifyNoInteractions(readingInstanceRepository, bookRepository);
    }

    @Test
    void execute_whenNoReadingInstanceExists_shouldComputeWantToReadShelf() {
        UUID bookId = UUID.randomUUID();
        UserBook ub = userBook(bookId);

        when(userBookRepository.findAllByUserId(userId)).thenReturn(List.of(ub));
        when(readingInstanceRepository.findLatestByUserBookIds(List.of(ub.getId()))).thenReturn(Map.of());
        when(bookRepository.findByIds(List.of(bookId))).thenReturn(List.of(book(bookId, "Dune")));

        List<LibraryItemOutput> result = useCase.execute(new GetUserLibraryInput(userId, null));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).shelf()).isEqualTo(Shelf.WANT_TO_READ);
        assertThat(result.get(0).title()).isEqualTo("Dune");
    }

    @Test
    void execute_shouldComputeShelfFromLatestReadingInstanceStatus() {
        UUID bookId = UUID.randomUUID();
        UserBook ub = userBook(bookId);

        ReadingInstance latest = new ReadingInstance();
        latest.setStatus(ReadingStatus.PAUSED);

        when(userBookRepository.findAllByUserId(userId)).thenReturn(List.of(ub));
        when(readingInstanceRepository.findLatestByUserBookIds(List.of(ub.getId())))
                .thenReturn(Map.of(ub.getId(), latest));
        when(bookRepository.findByIds(List.of(bookId))).thenReturn(List.of(book(bookId, "Dune")));

        List<LibraryItemOutput> result = useCase.execute(new GetUserLibraryInput(userId, null));

        assertThat(result.get(0).shelf()).isEqualTo(Shelf.PAUSED);
    }

    @Test
    void execute_shouldFilterByRequestedShelf() {
        UUID readingBookId = UUID.randomUUID();
        UUID wantToReadBookId = UUID.randomUUID();
        UserBook readingUb = userBook(readingBookId);
        UserBook wantToReadUb = userBook(wantToReadBookId);

        ReadingInstance readingInstance = new ReadingInstance();
        readingInstance.setStatus(ReadingStatus.READING);

        when(userBookRepository.findAllByUserId(userId)).thenReturn(List.of(readingUb, wantToReadUb));
        when(readingInstanceRepository.findLatestByUserBookIds(any()))
                .thenReturn(Map.of(readingUb.getId(), readingInstance));
        when(bookRepository.findByIds(any())).thenReturn(List.of(
                book(readingBookId, "Reading Book"),
                book(wantToReadBookId, "Want To Read Book")
        ));

        List<LibraryItemOutput> result = useCase.execute(new GetUserLibraryInput(userId, Shelf.WANT_TO_READ));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Want To Read Book");
    }

    @Test
    void execute_whenBookMissingFromBatchLookup_shouldSkipItDefensively() {
        UUID bookId = UUID.randomUUID();
        UserBook ub = userBook(bookId);

        when(userBookRepository.findAllByUserId(userId)).thenReturn(List.of(ub));
        when(readingInstanceRepository.findLatestByUserBookIds(any())).thenReturn(Map.of());
        when(bookRepository.findByIds(any())).thenReturn(List.of()); // data-integrity edge case

        List<LibraryItemOutput> result = useCase.execute(new GetUserLibraryInput(userId, null));

        assertThat(result).isEmpty();
    }

    @Test
    void execute_shouldUseBatchQueries_notOnePerBook() {
        UUID book1 = UUID.randomUUID();
        UUID book2 = UUID.randomUUID();
        UserBook ub1 = userBook(book1);
        UserBook ub2 = userBook(book2);

        when(userBookRepository.findAllByUserId(userId)).thenReturn(List.of(ub1, ub2));
        when(readingInstanceRepository.findLatestByUserBookIds(any())).thenReturn(Map.of());
        when(bookRepository.findByIds(any())).thenReturn(List.of(book(book1, "A"), book(book2, "B")));

        useCase.execute(new GetUserLibraryInput(userId, null));

        verify(readingInstanceRepository, times(1)).findLatestByUserBookIds(any());
        verify(bookRepository, times(1)).findByIds(any());
    }
}
