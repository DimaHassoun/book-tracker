package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.domain.exception.InvalidReadingStatusException;
import com.booktracker.book_tracker.domain.valueobject.BookFormat;
import com.booktracker.book_tracker.domain.valueobject.ExternalSource;
import com.booktracker.book_tracker.domain.valueobject.ReadingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddBookAndStartReadingUseCaseTest {

    @Mock
    private AddBookToLibraryUseCase addBookToLibraryUseCase;

    @Mock
    private StartReadingInstanceUseCase startReadingInstanceUseCase;

    private AddBookAndStartReadingUseCase useCase;

    private UUID userId;
    private UUID bookId;
    private UUID userBookId;

    @BeforeEach
    void setUp() {
        useCase = new AddBookAndStartReadingUseCase(addBookToLibraryUseCase, startReadingInstanceUseCase);
        userId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        userBookId = UUID.randomUUID();
    }

    private AddBookAndStartReadingInput input(ReadingStatus status, Boolean confirmReread) {
        return new AddBookAndStartReadingInput(
                userId,
                ExternalSource.GOOGLE_BOOKS,
                "gb-123",
                null, null,
                "Dune",
                null,
                List.of("Frank Herbert"),
                null, null, null, null, null, null, null,
                BookFormat.PHYSICAL,
                status,
                null,
                null,
                null,
                confirmReread
        );
    }

    @Test
    void execute_withNullStatus_shouldThrowInvalidReadingStatusExceptionWithoutTouchingLibrary() {
        assertThrows(InvalidReadingStatusException.class, () -> useCase.execute(input(null, null)));
        verifyNoInteractions(addBookToLibraryUseCase, startReadingInstanceUseCase);
    }

    @Test
    void execute_withValidInput_shouldAddToLibraryThenStartReadingInstance() {
        AddBookToLibraryOutput libraryOutput = new AddBookToLibraryOutput(
                userBookId, bookId, BookFormat.PHYSICAL, Instant.now(), true);
        when(addBookToLibraryUseCase.execute(any(AddBookToLibraryInput.class))).thenReturn(libraryOutput);

        StartReadingInstanceOutput instanceOutput = new StartReadingInstanceOutput(
                UUID.randomUUID(), userBookId, 1, ReadingStatus.READING, null, LocalDate.now(), null, Instant.now());
        when(startReadingInstanceUseCase.execute(any(StartReadingInstanceInput.class))).thenReturn(instanceOutput);

        AddBookAndStartReadingOutput output = useCase.execute(input(ReadingStatus.READING, null));

        assertThat(output.userBookId()).isEqualTo(userBookId);
        assertThat(output.bookId()).isEqualTo(bookId);
        assertThat(output.readNumber()).isEqualTo(1);
        assertThat(output.isReread()).isFalse();

        ArgumentCaptor<StartReadingInstanceInput> captor = ArgumentCaptor.forClass(StartReadingInstanceInput.class);
        verify(startReadingInstanceUseCase).execute(captor.capture());
        assertThat(captor.getValue().userBookId()).isEqualTo(userBookId);
        assertThat(captor.getValue().status()).isEqualTo(ReadingStatus.READING);
    }

    @Test
    void execute_shouldMarkIsRereadTrue_whenReadNumberGreaterThanOne() {
        AddBookToLibraryOutput libraryOutput = new AddBookToLibraryOutput(
                userBookId, bookId, BookFormat.PHYSICAL, Instant.now(), false);
        when(addBookToLibraryUseCase.execute(any())).thenReturn(libraryOutput);

        StartReadingInstanceOutput instanceOutput = new StartReadingInstanceOutput(
                UUID.randomUUID(), userBookId, 2, ReadingStatus.READING, null, LocalDate.now(), null, Instant.now());
        when(startReadingInstanceUseCase.execute(any())).thenReturn(instanceOutput);

        AddBookAndStartReadingOutput output = useCase.execute(input(ReadingStatus.READING, true));

        assertThat(output.isReread()).isTrue();
        assertThat(output.readNumber()).isEqualTo(2);
    }

    @Test
    void execute_shouldPropagateConfirmRerereadFlagToStartReadingInstanceInput() {
        when(addBookToLibraryUseCase.execute(any())).thenReturn(
                new AddBookToLibraryOutput(userBookId, bookId, BookFormat.PHYSICAL, Instant.now(), false));
        when(startReadingInstanceUseCase.execute(any())).thenReturn(
                new StartReadingInstanceOutput(UUID.randomUUID(), userBookId, 2, ReadingStatus.READING, null, LocalDate.now(), null, Instant.now()));

        useCase.execute(input(ReadingStatus.READING, true));

        ArgumentCaptor<StartReadingInstanceInput> captor = ArgumentCaptor.forClass(StartReadingInstanceInput.class);
        verify(startReadingInstanceUseCase).execute(captor.capture());
        assertThat(captor.getValue().confirmReread()).isTrue();
    }
}
