package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.domain.exception.ActiveReadingInstanceExistsException;
import com.booktracker.book_tracker.domain.exception.UserBookNotFoundException;
import com.booktracker.book_tracker.domain.model.ReadingInstance;
import com.booktracker.book_tracker.domain.model.UserBook;
import com.booktracker.book_tracker.domain.repository.ReadingInstanceRepository;
import com.booktracker.book_tracker.domain.repository.UserBookRepository;
import com.booktracker.book_tracker.domain.valueobject.ReadingStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class StartReadingInstanceUseCase {

    private final UserBookRepository userBookRepository;
    private final ReadingInstanceRepository readingInstanceRepository;

    public StartReadingInstanceUseCase(UserBookRepository userBookRepository,
                                        ReadingInstanceRepository readingInstanceRepository) {
        this.userBookRepository = userBookRepository;
        this.readingInstanceRepository = readingInstanceRepository;
    }

    public StartReadingInstanceOutput execute(StartReadingInstanceInput input) {
        UserBook userBook = userBookRepository.findById(input.userBookId())
                .filter(ub -> ub.getUserId().equals(input.userId()))
                .orElseThrow(() -> new UserBookNotFoundException(
                        "No library entry " + input.userBookId() + " found for this user"));

        if (input.status() == ReadingStatus.READING
                && readingInstanceRepository.existsActiveReadingInstance(userBook.getId())) {
            throw new ActiveReadingInstanceExistsException(
                    "This book already has an active READING instance");
        }

        int nextReadNumber = readingInstanceRepository
                .findMaxReadNumberByUserBookId(userBook.getId())
                .map(max -> max + 1)
                .orElse(1);

        // start_date: only auto-set for READING, and only if the user didn't already supply one
        LocalDate startDate = input.startDate();
        if (startDate == null && input.status() == ReadingStatus.READING) {
            startDate = LocalDate.now();
        }

        // end_date: only auto-set for READ/DNF, and only if the user didn't already supply one
        LocalDate endDate = input.endDate();
        if (endDate == null && (input.status() == ReadingStatus.READ || input.status() == ReadingStatus.DNF)) {
            endDate = LocalDate.now();
        }

        ReadingInstance instance = new ReadingInstance();
        instance.setUserBookId(userBook.getId());
        instance.setReadNumber(nextReadNumber);
        instance.setStatus(input.status());
        instance.setCurrentPage(input.currentPage()); 
        instance.setStartDate(startDate);
        instance.setEndDate(endDate);

        ReadingInstance saved = readingInstanceRepository.save(instance);

        return new StartReadingInstanceOutput(
                saved.getId(),
                saved.getUserBookId(),
                saved.getReadNumber(),
                saved.getStatus(),
                saved.getCurrentPage(),
                saved.getStartDate(),
                saved.getEndDate(),
                saved.getCreatedAt()
        );
    }
}