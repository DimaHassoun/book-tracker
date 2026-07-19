package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.domain.exception.InvalidReadingStatusException;
import org.springframework.stereotype.Service;

@Service
public class AddBookAndStartReadingUseCase {

    private final AddBookToLibraryUseCase addBookToLibraryUseCase;
    private final StartReadingInstanceUseCase startReadingInstanceUseCase;

    public AddBookAndStartReadingUseCase(AddBookToLibraryUseCase addBookToLibraryUseCase,
                                          StartReadingInstanceUseCase startReadingInstanceUseCase) {
        this.addBookToLibraryUseCase = addBookToLibraryUseCase;
        this.startReadingInstanceUseCase = startReadingInstanceUseCase;
    }

    public AddBookAndStartReadingOutput execute(AddBookAndStartReadingInput input) {
        if (input.status() == null) {
            throw new InvalidReadingStatusException(
                    "A reading status is required to start a reading instance. "
                            + "To save a book for later without tracking a read, add it to the library directly.");
        }

        AddBookToLibraryOutput libraryEntry = addBookToLibraryUseCase.execute(toLibraryInput(input));

        // All reread/resume/no-op/correction rules live in StartReadingInstanceUseCase
        // and apply identically here — nothing is re-implemented or bypassed.
        StartReadingInstanceInput startInput = new StartReadingInstanceInput(
                input.userId(),
                libraryEntry.userBookId(),
                input.status(),
                input.currentPage(),
                input.startDate(),
                input.endDate(),
                input.confirmReread()
        );

        StartReadingInstanceOutput instance = startReadingInstanceUseCase.execute(startInput);

        return new AddBookAndStartReadingOutput(
                libraryEntry.userBookId(),
                libraryEntry.bookId(),
                libraryEntry.ownedFormat(),
                instance.id(),
                instance.readNumber(),
                instance.status(),
                instance.currentPage(),
                instance.startDate(),
                instance.endDate(),
                instance.readNumber() > 1,
                instance.createdAt()
        );
    }

    private AddBookToLibraryInput toLibraryInput(AddBookAndStartReadingInput input) {
        return new AddBookToLibraryInput(
                input.userId(),
                input.externalSource(),
                input.externalId(),
                input.isbn13(),
                input.isbn10(),
                input.title(),
                input.subtitle(),
                input.authors(),
                input.publisher(),
                input.publishedDate(),
                input.description(),
                input.pageCount(),
                input.genres(),
                input.coverImageUrl(),
                input.language(),
                input.ownedFormat()
        );
    }
}