package com.booktracker.book_tracker.presentation.controller;

import com.booktracker.book_tracker.application.usecase.library.AddBookToLibraryInput;
import com.booktracker.book_tracker.application.usecase.library.AddBookToLibraryUseCase;
import com.booktracker.book_tracker.presentation.dto.request.AddToLibraryRequest;
import com.booktracker.book_tracker.presentation.dto.response.UserBookResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/library")
public class LibraryController {

    private final AddBookToLibraryUseCase addBookToLibraryUseCase;

    public LibraryController(AddBookToLibraryUseCase addBookToLibraryUseCase) {
        this.addBookToLibraryUseCase = addBookToLibraryUseCase;
    }

    @PostMapping
    public ResponseEntity<UserBookResponse> addToLibrary(
            @RequestBody AddToLibraryRequest request,
            Authentication authentication) {

        UUID userId = (UUID) authentication.getPrincipal();

        var input = new AddBookToLibraryInput(
                userId,
                request.externalSource(),
                request.externalId(),
                request.isbn13(),
                request.isbn10(),
                request.title(),
                request.subtitle(),
                request.authors(),
                request.publisher(),
                request.publishedDate(),
                request.description(),
                request.pageCount(),
                request.genres(),
                request.coverImageUrl(),
                request.language(),
                request.ownedFormat()
        );

        var output = addBookToLibraryUseCase.execute(input);

        return ResponseEntity.status(HttpStatus.CREATED).body(new UserBookResponse(
                output.userBookId(),
                output.bookId(),
                output.ownedFormat(),
                output.createdAt()
        ));
    }
}