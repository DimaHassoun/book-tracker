package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.domain.exception.InvalidReadingStatusException;
import com.booktracker.book_tracker.domain.exception.RereadConfirmationRequiredException;
import com.booktracker.book_tracker.domain.exception.UserBookNotFoundException;
import com.booktracker.book_tracker.domain.model.ReadingInstance;
import com.booktracker.book_tracker.domain.model.UserBook;
import com.booktracker.book_tracker.domain.repository.ReadingInstanceRepository;
import com.booktracker.book_tracker.domain.repository.UserBookRepository;
import com.booktracker.book_tracker.domain.valueobject.ReadingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartReadingInstanceUseCaseTest {

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private ReadingInstanceRepository readingInstanceRepository;

    @Mock
    private ApplyStatusTransitionUseCase applyStatusTransitionUseCase;

    private StartReadingInstanceUseCase useCase;

    private UUID userId;
    private UUID userBookId;
    private UserBook userBook;

    @BeforeEach
    void setUp() {
        useCase = new StartReadingInstanceUseCase(userBookRepository, readingInstanceRepository, applyStatusTransitionUseCase);
        userId = UUID.randomUUID();
        userBookId = UUID.randomUUID();
        userBook = new UserBook();
        userBook.setId(userBookId);
        userBook.setUserId(userId);
    }

    private StartReadingInstanceInput input(ReadingStatus status, Boolean confirmReread) {
        return new StartReadingInstanceInput(userId, userBookId, status, null, null, null, confirmReread);
    }

    private ReadingInstance instanceWithStatus(int readNumber, ReadingStatus status) {
        ReadingInstance instance = new ReadingInstance();
        instance.setId(UUID.randomUUID());
        instance.setUserBookId(userBookId);
        instance.setReadNumber(readNumber);
        instance.setStatus(status);
        return instance;
    }

    @Test
    void execute_withNullStatus_shouldThrowInvalidReadingStatusException() {
        assertThrows(InvalidReadingStatusException.class, () -> useCase.execute(input(null, null)));
        verifyNoInteractions(userBookRepository, readingInstanceRepository, applyStatusTransitionUseCase);
    }

    @Test
    void execute_whenUserBookNotFound_shouldThrowUserBookNotFoundException() {
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.empty());

        assertThrows(UserBookNotFoundException.class, () -> useCase.execute(input(ReadingStatus.READING, null)));
    }

    @Test
    void execute_whenUserBookBelongsToDifferentUser_shouldThrowUserBookNotFoundException() {
        userBook.setUserId(UUID.randomUUID());
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.of(userBook));

        assertThrows(UserBookNotFoundException.class, () -> useCase.execute(input(ReadingStatus.READING, null)));
    }

    @Test
    void execute_whenNoExistingInstance_andStatusReading_shouldCreateFirstInstanceWithStartDateToday() {
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.of(userBook));
        when(readingInstanceRepository.findLatestByUserBookId(userBookId)).thenReturn(Optional.empty());
        when(readingInstanceRepository.save(any(ReadingInstance.class))).thenAnswer(inv -> inv.getArgument(0));

        StartReadingInstanceOutput output = useCase.execute(input(ReadingStatus.READING, null));

        assertThat(output.readNumber()).isEqualTo(1);
        assertThat(output.status()).isEqualTo(ReadingStatus.READING);
        assertThat(output.startDate()).isEqualTo(LocalDate.now());
        assertThat(output.endDate()).isNull();
        verify(applyStatusTransitionUseCase, never()).execute(any(), any(), any(), any(), any());
    }

    @Test
    void execute_whenNoExistingInstance_andStatusPaused_shouldNotAutoSetAnyDates() {
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.of(userBook));
        when(readingInstanceRepository.findLatestByUserBookId(userBookId)).thenReturn(Optional.empty());
        when(readingInstanceRepository.save(any(ReadingInstance.class))).thenAnswer(inv -> inv.getArgument(0));

        StartReadingInstanceOutput output = useCase.execute(input(ReadingStatus.PAUSED, null));

        assertThat(output.startDate()).isNull();
        assertThat(output.endDate()).isNull();
    }

    @Test
    void execute_whenNoExistingInstance_andStatusRead_shouldSetEndDateToday() {
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.of(userBook));
        when(readingInstanceRepository.findLatestByUserBookId(userBookId)).thenReturn(Optional.empty());
        when(readingInstanceRepository.save(any(ReadingInstance.class))).thenAnswer(inv -> inv.getArgument(0));

        StartReadingInstanceOutput output = useCase.execute(input(ReadingStatus.READ, null));

        assertThat(output.endDate()).isEqualTo(LocalDate.now());
        assertThat(output.startDate()).isNull();
    }

    @Test
    void execute_whenLatestSameStatus_shouldBeNoOpAndNotSaveOrCallApplyTransition() {
        ReadingInstance latest = instanceWithStatus(1, ReadingStatus.READING);
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.of(userBook));
        when(readingInstanceRepository.findLatestByUserBookId(userBookId)).thenReturn(Optional.of(latest));

        StartReadingInstanceOutput output = useCase.execute(input(ReadingStatus.READING, null));

        assertThat(output.id()).isEqualTo(latest.getId());
        verify(readingInstanceRepository, never()).save(any());
        verify(applyStatusTransitionUseCase, never()).execute(any(), any(), any(), any(), any());
    }

    @Test
    void execute_whenLatestTerminalAndInputTerminalDifferent_shouldTreatAsCorrectionViaApplyTransition() {
        ReadingInstance latest = instanceWithStatus(1, ReadingStatus.DNF);
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.of(userBook));
        when(readingInstanceRepository.findLatestByUserBookId(userBookId)).thenReturn(Optional.of(latest));

        ReadingInstance corrected = instanceWithStatus(1, ReadingStatus.READ);
        when(applyStatusTransitionUseCase.execute(eq(latest), eq(ReadingStatus.READ), any(), any(), any()))
                .thenReturn(corrected);

        StartReadingInstanceOutput output = useCase.execute(input(ReadingStatus.READ, null));

        assertThat(output.readNumber()).isEqualTo(1);
        verify(readingInstanceRepository, never()).save(any(ReadingInstance.class));
    }

    @Test
    void execute_whenLatestTerminalAndInputActive_withoutConfirmReread_shouldThrowRereadConfirmationRequired() {
        ReadingInstance latest = instanceWithStatus(1, ReadingStatus.READ);
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.of(userBook));
        when(readingInstanceRepository.findLatestByUserBookId(userBookId)).thenReturn(Optional.of(latest));

        assertThrows(RereadConfirmationRequiredException.class,
                () -> useCase.execute(input(ReadingStatus.READING, null)));

        assertThrows(RereadConfirmationRequiredException.class,
                () -> useCase.execute(input(ReadingStatus.READING, false)));

        verify(readingInstanceRepository, never()).save(any());
    }

    @Test
    void execute_whenLatestTerminalAndInputActive_withConfirmReread_shouldCreateNewInstanceWithIncrementedReadNumber() {
        ReadingInstance latest = instanceWithStatus(1, ReadingStatus.READ);
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.of(userBook));
        when(readingInstanceRepository.findLatestByUserBookId(userBookId)).thenReturn(Optional.of(latest));
        when(readingInstanceRepository.save(any(ReadingInstance.class))).thenAnswer(inv -> inv.getArgument(0));

        StartReadingInstanceOutput output = useCase.execute(input(ReadingStatus.READING, true));

        assertThat(output.readNumber()).isEqualTo(2);
        assertThat(output.status()).isEqualTo(ReadingStatus.READING);
        assertThat(output.startDate()).isEqualTo(LocalDate.now());

        ArgumentCaptor<ReadingInstance> captor = ArgumentCaptor.forClass(ReadingInstance.class);
        verify(readingInstanceRepository).save(captor.capture());
        assertThat(captor.getValue().getReadNumber()).isEqualTo(2);
        verify(applyStatusTransitionUseCase, never()).execute(any(), any(), any(), any(), any());
    }

    @Test
    void execute_whenLatestNonTerminalDifferentStatus_shouldApplyTransitionInPlace_resumeCase() {
        // PAUSED -> READING is a resume, not a reread: must not increment read_number.
        ReadingInstance latest = instanceWithStatus(1, ReadingStatus.PAUSED);
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.of(userBook));
        when(readingInstanceRepository.findLatestByUserBookId(userBookId)).thenReturn(Optional.of(latest));

        ReadingInstance resumed = instanceWithStatus(1, ReadingStatus.READING);
        when(applyStatusTransitionUseCase.execute(eq(latest), eq(ReadingStatus.READING), any(), any(), any()))
                .thenReturn(resumed);

        StartReadingInstanceOutput output = useCase.execute(input(ReadingStatus.READING, null));

        assertThat(output.readNumber()).isEqualTo(1);
        assertThat(output.status()).isEqualTo(ReadingStatus.READING);
        verify(readingInstanceRepository, never()).save(any(ReadingInstance.class));
    }

    @Test
    void execute_whenLatestNonTerminalDifferentStatus_shouldApplyTransitionInPlace_pauseCase() {
        ReadingInstance latest = instanceWithStatus(1, ReadingStatus.READING);
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.of(userBook));
        when(readingInstanceRepository.findLatestByUserBookId(userBookId)).thenReturn(Optional.of(latest));

        ReadingInstance paused = instanceWithStatus(1, ReadingStatus.PAUSED);
        when(applyStatusTransitionUseCase.execute(eq(latest), eq(ReadingStatus.PAUSED), any(), any(), any()))
                .thenReturn(paused);

        StartReadingInstanceOutput output = useCase.execute(input(ReadingStatus.PAUSED, null));

        assertThat(output.status()).isEqualTo(ReadingStatus.PAUSED);
        assertThat(output.readNumber()).isEqualTo(1);
    }
}
