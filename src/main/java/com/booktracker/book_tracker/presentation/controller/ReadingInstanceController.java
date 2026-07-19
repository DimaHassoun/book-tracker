package com.booktracker.book_tracker.presentation.controller;

import com.booktracker.book_tracker.application.usecase.library.StartReadingInstanceOutput;
import com.booktracker.book_tracker.application.usecase.library.UpdateReadingStatusInput;
import com.booktracker.book_tracker.application.usecase.library.UpdateReadingStatusUseCase;
import com.booktracker.book_tracker.application.usecase.session.LogReadingSessionInput;
import com.booktracker.book_tracker.application.usecase.session.LogReadingSessionOutput;
import com.booktracker.book_tracker.application.usecase.session.LogReadingSessionUseCase;
import com.booktracker.book_tracker.presentation.dto.request.LogReadingSessionRequest;
import com.booktracker.book_tracker.presentation.dto.request.UpdateReadingStatusRequest;
import com.booktracker.book_tracker.presentation.dto.response.ReadingInstanceResponse;
import com.booktracker.book_tracker.presentation.dto.response.ReadingSessionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reading-instances")
public class ReadingInstanceController {

    private final UpdateReadingStatusUseCase updateReadingStatusUseCase;
    private final LogReadingSessionUseCase logReadingSessionUseCase;

    public ReadingInstanceController(UpdateReadingStatusUseCase updateReadingStatusUseCase,
                                      LogReadingSessionUseCase logReadingSessionUseCase) {
        this.updateReadingStatusUseCase = updateReadingStatusUseCase;
        this.logReadingSessionUseCase = logReadingSessionUseCase;
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ReadingInstanceResponse> updateStatus(
            @PathVariable UUID id,
            @RequestBody UpdateReadingStatusRequest request,
            Authentication authentication) {

        UUID userId = (UUID) authentication.getPrincipal();

        var input = new UpdateReadingStatusInput(
                userId,
                id,
                request.status(),
                request.currentPage(),
                request.startDate(),
                request.endDate()
        );

        StartReadingInstanceOutput output = updateReadingStatusUseCase.execute(input);

        return ResponseEntity.ok(new ReadingInstanceResponse(
                output.id(),
                output.userBookId(),
                output.readNumber(),
                output.status(),
                output.currentPage(),
                output.startDate(),
                output.endDate(),
                output.createdAt()
        ));
    }

    @PostMapping("/{id}/sessions")
    public ResponseEntity<ReadingSessionResponse> logSession(
            @PathVariable UUID id,
            @RequestBody LogReadingSessionRequest request,
            Authentication authentication) {

        UUID userId = (UUID) authentication.getPrincipal();

        var input = new LogReadingSessionInput(
                userId,
                id,
                request.sessionDate(),
                request.startPage(),
                request.endPage(),
                request.durationMinutes()
        );

        LogReadingSessionOutput output = logReadingSessionUseCase.execute(input);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ReadingSessionResponse(
                output.id(),
                output.readingInstanceId(),
                output.sessionDate(),
                output.startPage(),
                output.endPage(),
                output.durationMinutes(),
                output.pagesRead(),
                output.createdAt()
        ));
    }
}