package com.booktracker.book_tracker.application.usecase.session;

import com.booktracker.book_tracker.domain.exception.InvalidReadingSessionException;
import com.booktracker.book_tracker.domain.exception.ReadingInstanceNotFoundException;
import com.booktracker.book_tracker.domain.model.ReadingInstance;
import com.booktracker.book_tracker.domain.model.ReadingSession;
import com.booktracker.book_tracker.domain.model.UserBook;
import com.booktracker.book_tracker.domain.repository.ReadingInstanceRepository;
import com.booktracker.book_tracker.domain.repository.ReadingSessionRepository;
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
class LogReadingSessionUseCaseTest {

    @Mock
    private ReadingInstanceRepository readingInstanceRepository;

    @Mock
    private UserBookRepository userBookRepository;

    @Mock
    private ReadingSessionRepository readingSessionRepository;

    private LogReadingSessionUseCase useCase;

    private UUID userId;
    private UUID instanceId;
    private UUID userBookId;
    private ReadingInstance instance;
    private UserBook userBook;

    @BeforeEach
    void setUp() {
        useCase = new LogReadingSessionUseCase(readingInstanceRepository, userBookRepository, readingSessionRepository);
        userId = UUID.randomUUID();
        instanceId = UUID.randomUUID();
        userBookId = UUID.randomUUID();

        instance = new ReadingInstance();
        instance.setId(instanceId);
        instance.setUserBookId(userBookId);
        instance.setStatus(ReadingStatus.READING);

        userBook = new UserBook();
        userBook.setId(userBookId);
        userBook.setUserId(userId);
    }

    private LogReadingSessionInput input(LocalDate date, Integer start, Integer end, Integer duration) {
        return new LogReadingSessionInput(userId, instanceId, date, start, end, duration);
    }

    private void stubInstanceAndOwnerFound() {
        when(readingInstanceRepository.findById(instanceId)).thenReturn(Optional.of(instance));
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.of(userBook));
    }

    @Test
    void execute_withNullStartPage_shouldThrowInvalidReadingSessionException() {
        assertThrows(InvalidReadingSessionException.class,
                () -> useCase.execute(input(LocalDate.now(), null, 50, null)));
        verifyNoInteractions(readingInstanceRepository, readingSessionRepository);
    }

    @Test
    void execute_withNegativeStartPage_shouldThrowInvalidReadingSessionException() {
        assertThrows(InvalidReadingSessionException.class,
                () -> useCase.execute(input(LocalDate.now(), -1, 50, null)));
    }

    @Test
    void execute_withNullEndPage_shouldThrowInvalidReadingSessionException() {
        assertThrows(InvalidReadingSessionException.class,
                () -> useCase.execute(input(LocalDate.now(), 10, null, null)));
    }

    @Test
    void execute_withEndPageNotGreaterThanStartPage_shouldThrowInvalidReadingSessionException() {
        assertThrows(InvalidReadingSessionException.class,
                () -> useCase.execute(input(LocalDate.now(), 50, 50, null)));
        assertThrows(InvalidReadingSessionException.class,
                () -> useCase.execute(input(LocalDate.now(), 50, 40, null)));
    }

    @Test
    void execute_withFutureSessionDate_shouldThrowInvalidReadingSessionException() {
        assertThrows(InvalidReadingSessionException.class,
                () -> useCase.execute(input(LocalDate.now().plusDays(1), 10, 50, null)));
    }

    @Test
    void execute_withNonPositiveDuration_shouldThrowInvalidReadingSessionException() {
        assertThrows(InvalidReadingSessionException.class,
                () -> useCase.execute(input(LocalDate.now(), 10, 50, 0)));
        assertThrows(InvalidReadingSessionException.class,
                () -> useCase.execute(input(LocalDate.now(), 10, 50, -5)));
    }

    @Test
    void execute_whenInstanceNotFound_shouldThrowReadingInstanceNotFoundException() {
        when(readingInstanceRepository.findById(instanceId)).thenReturn(Optional.empty());

        assertThrows(ReadingInstanceNotFoundException.class,
                () -> useCase.execute(input(LocalDate.now(), 10, 50, null)));

        verifyNoInteractions(readingSessionRepository);
    }

    @Test
    void execute_whenUserBookNotOwnedByCaller_shouldThrowReadingInstanceNotFoundException() {
        userBook.setUserId(UUID.randomUUID());
        when(readingInstanceRepository.findById(instanceId)).thenReturn(Optional.of(instance));
        when(userBookRepository.findById(userBookId)).thenReturn(Optional.of(userBook));

        assertThrows(ReadingInstanceNotFoundException.class,
                () -> useCase.execute(input(LocalDate.now(), 10, 50, null)));
    }

    @Test
    void execute_withValidInput_shouldSaveSessionAndReturnPagesRead() {
        stubInstanceAndOwnerFound();
        instance.setCurrentPage(null);
        when(readingSessionRepository.save(any(ReadingSession.class))).thenAnswer(inv -> {
            ReadingSession s = inv.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        LogReadingSessionOutput output = useCase.execute(input(LocalDate.now(), 10, 60, 45));

        assertThat(output.startPage()).isEqualTo(10);
        assertThat(output.endPage()).isEqualTo(60);
        assertThat(output.pagesRead()).isEqualTo(50);
        assertThat(output.durationMinutes()).isEqualTo(45);
    }

    @Test
    void execute_withNullSessionDate_shouldDefaultToToday() {
        stubInstanceAndOwnerFound();
        when(readingSessionRepository.save(any(ReadingSession.class))).thenAnswer(inv -> inv.getArgument(0));

        LogReadingSessionOutput output = useCase.execute(input(null, 10, 60, null));

        assertThat(output.sessionDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void execute_whenCurrentPageIsNull_shouldAdvanceCurrentPageToSessionEndPage() {
        stubInstanceAndOwnerFound();
        instance.setCurrentPage(null);
        when(readingSessionRepository.save(any(ReadingSession.class))).thenAnswer(inv -> inv.getArgument(0));
        when(readingInstanceRepository.save(any(ReadingInstance.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(input(LocalDate.now(), 0, 40, null));

        ArgumentCaptor<ReadingInstance> captor = ArgumentCaptor.forClass(ReadingInstance.class);
        verify(readingInstanceRepository).save(captor.capture());
        assertThat(captor.getValue().getCurrentPage()).isEqualTo(40);
    }

    @Test
    void execute_whenSessionEndPageGreaterThanCurrentPage_shouldAdvanceCurrentPage() {
        stubInstanceAndOwnerFound();
        instance.setCurrentPage(100);
        when(readingSessionRepository.save(any(ReadingSession.class))).thenAnswer(inv -> inv.getArgument(0));
        when(readingInstanceRepository.save(any(ReadingInstance.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(input(LocalDate.now(), 100, 150, null));

        ArgumentCaptor<ReadingInstance> captor = ArgumentCaptor.forClass(ReadingInstance.class);
        verify(readingInstanceRepository).save(captor.capture());
        assertThat(captor.getValue().getCurrentPage()).isEqualTo(150);
    }

    @Test
    void execute_whenSessionEndPageNotGreaterThanCurrentPage_shouldNeverDecreaseCurrentPage() {
        stubInstanceAndOwnerFound();
        instance.setCurrentPage(200);
        when(readingSessionRepository.save(any(ReadingSession.class))).thenAnswer(inv -> inv.getArgument(0));

        // Logging an earlier/out-of-order session (e.g. re-reading a prior chapter).
        useCase.execute(input(LocalDate.now(), 10, 60, null));

        verify(readingInstanceRepository, never()).save(any(ReadingInstance.class));
        assertThat(instance.getCurrentPage()).isEqualTo(200);
    }

    @Test
    void execute_whenSessionEndPageEqualsCurrentPage_shouldNotResaveInstance() {
        stubInstanceAndOwnerFound();
        instance.setCurrentPage(60);
        when(readingSessionRepository.save(any(ReadingSession.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(input(LocalDate.now(), 10, 60, null));

        verify(readingInstanceRepository, never()).save(any(ReadingInstance.class));
    }
}
