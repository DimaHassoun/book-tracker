package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.domain.exception.InvalidReadingStatusException;
import com.booktracker.book_tracker.domain.exception.ReadingInstanceNotFoundException;
import com.booktracker.book_tracker.domain.model.ReadingInstance;
import com.booktracker.book_tracker.domain.model.UserBook;
import com.booktracker.book_tracker.domain.repository.ReadingInstanceRepository;
import com.booktracker.book_tracker.domain.repository.UserBookRepository;
import org.springframework.stereotype.Service;

/**
 * Directly corrects an existing reading instance's status — e.g. fixing a
 * mis-logged DNF to READ, or updating the page/dates on a specific
 * historical instance.
 *
 * Deliberately does NOT run reread/resume decision logic — that belongs
 * exclusively to StartReadingInstanceUseCase. This always updates the
 * instance identified by readingInstanceId in place via
 * ApplyStatusTransitionUseCase, regardless of whether it happens to be the
 * user_book's latest instance. A correction to reading instance #1's status
 * does not affect read_number or create a new instance, even if instance #2
 * already exists.
 */
@Service
public class UpdateReadingStatusUseCase {

    private final ReadingInstanceRepository readingInstanceRepository;
    private final UserBookRepository userBookRepository;
    private final ApplyStatusTransitionUseCase applyStatusTransitionUseCase;

    public UpdateReadingStatusUseCase(ReadingInstanceRepository readingInstanceRepository,
                                       UserBookRepository userBookRepository,
                                       ApplyStatusTransitionUseCase applyStatusTransitionUseCase) {
        this.readingInstanceRepository = readingInstanceRepository;
        this.userBookRepository = userBookRepository;
        this.applyStatusTransitionUseCase = applyStatusTransitionUseCase;
    }

    public StartReadingInstanceOutput execute(UpdateReadingStatusInput input) {
        if (input.status() == null) {
            throw new InvalidReadingStatusException("A reading status is required.");
        }

        ReadingInstance instance = readingInstanceRepository.findById(input.readingInstanceId())
                .orElseThrow(() -> new ReadingInstanceNotFoundException(
                        "No reading instance " + input.readingInstanceId() + " found for this user"));

        UserBook userBook = userBookRepository.findById(instance.getUserBookId())
                .filter(ub -> ub.getUserId().equals(input.userId()))
                .orElseThrow(() -> new ReadingInstanceNotFoundException(
                        "No reading instance " + input.readingInstanceId() + " found for this user"));

        ReadingInstance updated = applyStatusTransitionUseCase.execute(
                instance, input.status(), input.currentPage(), input.startDate(), input.endDate());

        return toOutput(updated);
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