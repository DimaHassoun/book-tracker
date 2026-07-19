package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.domain.exception.InvalidReadingStatusException;
import com.booktracker.book_tracker.domain.exception.RereadConfirmationRequiredException;
import com.booktracker.book_tracker.domain.exception.UserBookNotFoundException;
import com.booktracker.book_tracker.domain.model.ReadingInstance;
import com.booktracker.book_tracker.domain.model.UserBook;
import com.booktracker.book_tracker.domain.repository.ReadingInstanceRepository;
import com.booktracker.book_tracker.domain.repository.UserBookRepository;
import com.booktracker.book_tracker.domain.valueobject.ReadingStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class StartReadingInstanceUseCase {

    private static final Set<ReadingStatus> TERMINAL_STATUSES = Set.of(ReadingStatus.READ, ReadingStatus.DNF);

    private final UserBookRepository userBookRepository;
    private final ReadingInstanceRepository readingInstanceRepository;
    private final ApplyStatusTransitionUseCase applyStatusTransitionUseCase;

    public StartReadingInstanceUseCase(UserBookRepository userBookRepository,
                                        ReadingInstanceRepository readingInstanceRepository,
                                        ApplyStatusTransitionUseCase applyStatusTransitionUseCase) {
        this.userBookRepository = userBookRepository;
        this.readingInstanceRepository = readingInstanceRepository;
        this.applyStatusTransitionUseCase = applyStatusTransitionUseCase;
    }

    public StartReadingInstanceOutput execute(StartReadingInstanceInput input) {
        if (input.status() == null) {
            // Defensive only — the frontend always sends a status because the user
            // picked an explicit action button. A null status means a malformed
            // request, not a real user scenario.
            throw new InvalidReadingStatusException("A reading status is required.");
        }

        UserBook userBook = userBookRepository.findById(input.userBookId())
                .filter(ub -> ub.getUserId().equals(input.userId()))
                .orElseThrow(() -> new UserBookNotFoundException(
                        "No library entry " + input.userBookId() + " found for this user"));

        Optional<ReadingInstance> latest = readingInstanceRepository.findLatestByUserBookId(userBook.getId());

        ReadingInstance result = latest.isEmpty()
                ? createNewInstance(userBook.getId(), 1, input)
                : resolveTransition(userBook.getId(), latest.get(), input);

        return toOutput(result);
    }

    private ReadingInstance resolveTransition(UUID userBookId, ReadingInstance latestInstance,
                                               StartReadingInstanceInput input) {

        if (latestInstance.getStatus() == input.status()) {
            // Rule 5: same-status is always a no-op — even READ -> READ, which is
            // NOT how a reread is started (that requires the explicit confirmReread path below).
            return latestInstance;
        }

        if (isTerminal(latestInstance.getStatus())) {
            if (isTerminal(input.status())) {
                // READ <-> DNF: a correction to existing history, not a reread.
                return applyStatusTransitionUseCase.execute(
                        latestInstance, input.status(), input.currentPage(), input.startDate(), input.endDate());
            }

            // Terminal -> active (READING/PAUSED): a reread. Requires explicit confirmation.
            if (!Boolean.TRUE.equals(input.confirmReread())) {
                throw new RereadConfirmationRequiredException(
                        "This book has previous reading history. Set confirmReread=true to start a new read.");
            }
            return createNewInstance(userBookId, latestInstance.getReadNumber() + 1, input);
        }

        // Latest is READING/PAUSED (non-terminal): any different status is an in-place
        // continuation of the same attempt. Covers resume (PAUSED -> READING) and normal
        // progression (READING -> READ/DNF/PAUSED) identically — no special-casing needed.
        return applyStatusTransitionUseCase.execute(
                latestInstance, input.status(), input.currentPage(), input.startDate(), input.endDate());
    }

    private boolean isTerminal(ReadingStatus status) {
        return TERMINAL_STATUSES.contains(status);
    }

    private ReadingInstance createNewInstance(UUID userBookId, int readNumber, StartReadingInstanceInput input) {
        LocalDate startDate = input.startDate();
        if (startDate == null && input.status() == ReadingStatus.READING) {
            startDate = LocalDate.now();
        }

        LocalDate endDate = input.endDate();
        if (endDate == null && (input.status() == ReadingStatus.READ || input.status() == ReadingStatus.DNF)) {
            endDate = LocalDate.now();
        }

        ReadingInstance instance = new ReadingInstance();
        instance.setUserBookId(userBookId);
        instance.setReadNumber(readNumber);
        instance.setStatus(input.status());
        instance.setCurrentPage(input.currentPage());
        instance.setStartDate(startDate);
        instance.setEndDate(endDate);

        return readingInstanceRepository.save(instance);
    }

    private StartReadingInstanceOutput toOutput(ReadingInstance instance) {
        return new StartReadingInstanceOutput(
                instance.getId(),
                instance.getUserBookId(),
                instance.getReadNumber(),
                instance.getStatus(),
                instance.getCurrentPage(),
                instance.getStartDate(),
                instance.getEndDate(),
                instance.getCreatedAt()
        );
    }
}