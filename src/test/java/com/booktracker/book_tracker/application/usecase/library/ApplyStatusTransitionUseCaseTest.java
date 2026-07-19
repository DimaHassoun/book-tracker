package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.domain.model.ReadingInstance;
import com.booktracker.book_tracker.domain.model.StatusChangeLog;
import com.booktracker.book_tracker.domain.repository.ReadingInstanceRepository;
import com.booktracker.book_tracker.domain.repository.StatusChangeLogRepository;
import com.booktracker.book_tracker.domain.valueobject.ReadingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplyStatusTransitionUseCaseTest {

	@Mock
	private ReadingInstanceRepository readingInstanceRepository;

	@Mock
	private StatusChangeLogRepository statusChangeLogRepository;

	private ApplyStatusTransitionUseCase useCase;

	@BeforeEach
	void setUp() {
		useCase = new ApplyStatusTransitionUseCase(readingInstanceRepository, statusChangeLogRepository);
	}

	private void mockSaveEchoesInput() {
	    when(readingInstanceRepository.save(any(ReadingInstance.class)))
	            .thenAnswer(inv -> inv.getArgument(0));
	}

	private ReadingInstance instance(ReadingStatus status, Integer currentPage, LocalDate startDate, LocalDate endDate) {
		ReadingInstance instance = new ReadingInstance();
		instance.setId(UUID.randomUUID());
		instance.setUserBookId(UUID.randomUUID());
		instance.setReadNumber(1);
		instance.setStatus(status);
		instance.setCurrentPage(currentPage);
		instance.setStartDate(startDate);
		instance.setEndDate(endDate);
		return instance;
	}

	@Test
	void execute_whenSameStatus_shouldReturnUnchangedInstanceWithoutSavingOrLogging() {
		ReadingInstance instance = instance(ReadingStatus.PAUSED, 50, null, null);

		ReadingInstance result = useCase.execute(instance, ReadingStatus.PAUSED, null, null, null);

		assertThat(result).isSameAs(instance);
		verify(readingInstanceRepository, never()).save(any());
		verify(statusChangeLogRepository, never()).save(any());
	}

	@Test
	void execute_whenStatusChanges_shouldUpdateStatusAndPersistInstance() {
		mockSaveEchoesInput();
		ReadingInstance instance = instance(ReadingStatus.READING, 100, LocalDate.of(2026, 1, 1), null);

		ReadingInstance result = useCase.execute(instance, ReadingStatus.PAUSED, null, null, null);

		assertThat(result.getStatus()).isEqualTo(ReadingStatus.PAUSED);
		verify(readingInstanceRepository).save(instance);
	}

	@Test
	void execute_shouldLogOldAndNewStatus_withProvidedCurrentPageAsPageAtChange() {
		mockSaveEchoesInput();
		
		ReadingInstance instance = instance(ReadingStatus.READING, 100, LocalDate.of(2026, 1, 1), null);

		useCase.execute(instance, ReadingStatus.PAUSED, 155, null, null);

		ArgumentCaptor<StatusChangeLog> captor = ArgumentCaptor.forClass(StatusChangeLog.class);
		verify(statusChangeLogRepository).save(captor.capture());
		StatusChangeLog log = captor.getValue();

		assertThat(log.getOldStatus()).isEqualTo(ReadingStatus.READING);
		assertThat(log.getNewStatus()).isEqualTo(ReadingStatus.PAUSED);
		assertThat(log.getPageAtChange()).isEqualTo(155);
		assertThat(instance.getCurrentPage()).isEqualTo(155);
	}

	@Test
	void execute_whenCurrentPageNotProvided_shouldUsePreviousCurrentPageAsPageAtChange() {
		mockSaveEchoesInput();
		ReadingInstance instance = instance(ReadingStatus.READING, 100, LocalDate.of(2026, 1, 1), null);

		useCase.execute(instance, ReadingStatus.PAUSED, null, null, null);

		ArgumentCaptor<StatusChangeLog> captor = ArgumentCaptor.forClass(StatusChangeLog.class);
		verify(statusChangeLogRepository).save(captor.capture());

		assertThat(captor.getValue().getPageAtChange()).isEqualTo(100);
		assertThat(instance.getCurrentPage()).isEqualTo(100); // unchanged, since none was supplied
	}

	@Test
	void execute_whenTransitioningToReadingWithNoStartDate_shouldDefaultStartDateToToday() {
		mockSaveEchoesInput();
		ReadingInstance instance = instance(ReadingStatus.PAUSED, null, null, null);

		ReadingInstance result = useCase.execute(instance, ReadingStatus.READING, null, null, null);

		assertThat(result.getStartDate()).isEqualTo(LocalDate.now());
	}

	@Test
	void execute_whenExplicitStartDateProvided_shouldUseExplicitDateInsteadOfToday() {
		mockSaveEchoesInput();
		ReadingInstance instance = instance(ReadingStatus.PAUSED, null, null, null);
		LocalDate explicit = LocalDate.of(2025, 5, 1);

		ReadingInstance result = useCase.execute(instance, ReadingStatus.READING, null, explicit, null);

		assertThat(result.getStartDate()).isEqualTo(explicit);
	}

	@Test
	void execute_shouldNotOverwriteExistingStartDate_whenAlreadySetAndNoExplicitOverride() {
		mockSaveEchoesInput();
		LocalDate original = LocalDate.of(2025, 1, 1);
		ReadingInstance instance = instance(ReadingStatus.PAUSED, null, original, null);

		ReadingInstance result = useCase.execute(instance, ReadingStatus.READING, null, null, null);

		assertThat(result.getStartDate()).isEqualTo(original);
	}

	@Test
	void execute_whenTransitioningToReadWithNoEndDate_shouldDefaultEndDateToToday() {
		mockSaveEchoesInput();
		ReadingInstance instance = instance(ReadingStatus.READING, 200, LocalDate.of(2026, 1, 1), null);

		ReadingInstance result = useCase.execute(instance, ReadingStatus.READ, null, null, null);

		assertThat(result.getEndDate()).isEqualTo(LocalDate.now());
	}

	@Test
	void execute_whenTransitioningToDnfWithNoEndDate_shouldDefaultEndDateToToday() {
		mockSaveEchoesInput();
		ReadingInstance instance = instance(ReadingStatus.READING, 200, LocalDate.of(2026, 1, 1), null);

		ReadingInstance result = useCase.execute(instance, ReadingStatus.DNF, null, null, null);

		assertThat(result.getEndDate()).isEqualTo(LocalDate.now());
	}

	@Test
	void execute_whenExplicitEndDateProvided_shouldUseExplicitDate() {
		mockSaveEchoesInput();
		ReadingInstance instance = instance(ReadingStatus.READING, 200, LocalDate.of(2026, 1, 1), null);
		LocalDate explicit = LocalDate.of(2026, 3, 10);

		ReadingInstance result = useCase.execute(instance, ReadingStatus.READ, null, null, explicit);

		assertThat(result.getEndDate()).isEqualTo(explicit);
	}

	@Test
	void execute_correctionFromReadBackToDnf_shouldAllowExplicitEndDateOverride() {
		mockSaveEchoesInput();
		// READ <-> DNF corrections travel through this use case too (called directly
		// by StartReadingInstanceUseCase for terminal-to-terminal transitions).
		ReadingInstance instance = instance(ReadingStatus.READ, 300, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 15));
		LocalDate correctedEndDate = LocalDate.of(2026, 1, 20);

		ReadingInstance result = useCase.execute(instance, ReadingStatus.DNF, null, null, correctedEndDate);

		assertThat(result.getStatus()).isEqualTo(ReadingStatus.DNF);
		assertThat(result.getEndDate()).isEqualTo(correctedEndDate);
	}

}
