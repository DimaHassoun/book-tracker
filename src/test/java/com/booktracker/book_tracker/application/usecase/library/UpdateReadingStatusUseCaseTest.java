package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.domain.exception.InvalidReadingStatusException;
import com.booktracker.book_tracker.domain.exception.ReadingInstanceNotFoundException;
import com.booktracker.book_tracker.domain.model.ReadingInstance;
import com.booktracker.book_tracker.domain.model.UserBook;
import com.booktracker.book_tracker.domain.repository.ReadingInstanceRepository;
import com.booktracker.book_tracker.domain.repository.UserBookRepository;
import com.booktracker.book_tracker.domain.valueobject.ReadingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateReadingStatusUseCaseTest {

    @Mock
    private ReadingInstanceRepository readingInstanceRepository;

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private ApplyStatusTransitionUseCase applyStatusTransitionUseCase;

    private UpdateReadingStatusUseCase useCase;

    private UUID userId;
    private UUID instanceId;
    private UUID userBookId;
    private ReadingInstance instance;
    private UserBook userBook;

    @BeforeEach
    void setUp() {
        useCase = new UpdateReadingStatusUseCase(readingInstanceRepository, userBookRepository, applyStatusTransitionUseCase);
        userId = UUID.randomUUID();
        instanceId = UUID.randomUUID();
        userBookId = UUID.randomUUID();

        instance = new ReadingInstance();
        instance.setId(instanceId);
        instance.setUserBookId(userBookId);
        instance.setReadNumber(1);
        instance.setStatus(ReadingStatus.READING);

        userBook = new UserBook();
        userBook.setId(userBookId);
        userBook.setUserId(userId);
    }

    private UpdateReadingStatusInput input(ReadingStatus status) {
        return new UpdateReadingStatusInput(userId, instanceId, status, null, null, null);
    }

    @Test
    void execute_withNullStatus_shouldThrowInvalidReadingStatusException() {
        assertThrows(InvalidReadingStatusException.class, () -> useCase.execute(input(null)));
        verifyNoInteractions(readingInstanceRepository, userBookRepository, applyStatusTransitionUseCase);
    }

    @Test
    void execute_whenInstanceNotFound_shouldThrowReadingInstanceNotFoundException() {
        when(readingInstanceRepository.findById(instanceId)).thenReturn(Optional.empty());

        assertThrows(ReadingInstanceNotFoundException.class, () -> useCase.execute(input(ReadingStatus.PAUSED)));
    }

    @Test
    void execute_whenUserBookNotOwnedByCallingUser_shouldThrowReadingInstanceNotFoundException() {
        userBook.setUserId(UUID.randomUUID());
        when(readingInstanceRepository.findById(instanceId)).thenReturn(Optional.of(instance));
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.of(userBook));

        assertThrows(ReadingInstanceNotFoundException.class, () -> useCase.execute(input(ReadingStatus.PAUSED)));
    }

    @Test
    void execute_whenUserBookMissingEntirely_shouldThrowReadingInstanceNotFoundException() {
        when(readingInstanceRepository.findById(instanceId)).thenReturn(Optional.of(instance));
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.empty());

        assertThrows(ReadingInstanceNotFoundException.class, () -> useCase.execute(input(ReadingStatus.PAUSED)));
    }

    @Test
    void execute_withValidInput_shouldDelegateToApplyStatusTransitionAndReturnMappedOutput() {
        when(readingInstanceRepository.findById(instanceId)).thenReturn(Optional.of(instance));
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.of(userBook));

        ReadingInstance updated = new ReadingInstance();
        updated.setId(instanceId);
        updated.setUserBookId(userBookId);
        updated.setReadNumber(1);
        updated.setStatus(ReadingStatus.PAUSED);
        updated.setCurrentPage(77);

        when(applyStatusTransitionUseCase.execute(eq(instance), eq(ReadingStatus.PAUSED), any(), any(), any()))
                .thenReturn(updated);

        StartReadingInstanceOutput output = useCase.execute(
                new UpdateReadingStatusInput(userId, instanceId, ReadingStatus.PAUSED, 77, null, null));

        assertThat(output.status()).isEqualTo(ReadingStatus.PAUSED);
        assertThat(output.currentPage()).isEqualTo(77);
        assertThat(output.readNumber()).isEqualTo(1);
    }

    @Test
    void execute_shouldNotChangeReadNumber_evenWhenNewerReadingInstanceExistsForSameBook() {
        // A correction to instance #1 must never affect read_number/instance identity,
        // even if a reread (#2) already exists for the same user_book.
        when(readingInstanceRepository.findById(instanceId)).thenReturn(Optional.of(instance));
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.of(userBook));

        ReadingInstance corrected = new ReadingInstance();
        corrected.setId(instanceId);
        corrected.setUserBookId(userBookId);
        corrected.setReadNumber(1);
        corrected.setStatus(ReadingStatus.READ);

        when(applyStatusTransitionUseCase.execute(eq(instance), eq(ReadingStatus.READ), any(), any(), any()))
                .thenReturn(corrected);

        StartReadingInstanceOutput output = useCase.execute(input(ReadingStatus.READ));

        assertThat(output.readNumber()).isEqualTo(1);
        assertThat(output.id()).isEqualTo(instanceId);
    }
}
