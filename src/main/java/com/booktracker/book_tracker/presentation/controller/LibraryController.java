package com.booktracker.book_tracker.presentation.controller;

import com.booktracker.book_tracker.application.usecase.library.AddBookToLibraryInput;
import com.booktracker.book_tracker.application.usecase.library.AddBookToLibraryUseCase;
import com.booktracker.book_tracker.application.usecase.library.GetUserBookDetailInput;
import com.booktracker.book_tracker.application.usecase.library.GetUserBookDetailUseCase;
import com.booktracker.book_tracker.application.usecase.library.GetUserLibraryInput;
import com.booktracker.book_tracker.application.usecase.library.GetUserLibraryUseCase;
import com.booktracker.book_tracker.application.usecase.library.LibraryItemOutput;
import com.booktracker.book_tracker.application.usecase.library.ReadingInstanceSummary;
import com.booktracker.book_tracker.application.usecase.library.UserBookDetailOutput;
import com.booktracker.book_tracker.domain.valueobject.Shelf;
import com.booktracker.book_tracker.presentation.dto.request.AddToLibraryRequest;
import com.booktracker.book_tracker.presentation.dto.response.LibraryItemResponse;
import com.booktracker.book_tracker.presentation.dto.response.ReadingInstanceSummaryResponse;
import com.booktracker.book_tracker.presentation.dto.response.UserBookDetailResponse;
import com.booktracker.book_tracker.presentation.dto.response.UserBookResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.booktracker.book_tracker.presentation.dto.request.StartReadingInstanceRequest;
import com.booktracker.book_tracker.presentation.dto.response.ReadingInstanceResponse;
import com.booktracker.book_tracker.application.usecase.library.StartReadingInstanceInput;
import com.booktracker.book_tracker.application.usecase.library.StartReadingInstanceUseCase;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/library")
public class LibraryController {

	private final AddBookToLibraryUseCase addBookToLibraryUseCase;
	private final StartReadingInstanceUseCase startReadingInstanceUseCase;
	private final GetUserLibraryUseCase getUserLibraryUseCase;
	private final GetUserBookDetailUseCase getUserBookDetailUseCase;

	public LibraryController(
			AddBookToLibraryUseCase addBookToLibraryUseCase,
			StartReadingInstanceUseCase startReadingInstanceUseCase,
			GetUserLibraryUseCase getUserLibraryUseCase,
			GetUserBookDetailUseCase getUserBookDetailUseCase
			) {
		this.addBookToLibraryUseCase = addBookToLibraryUseCase;
		this.startReadingInstanceUseCase = startReadingInstanceUseCase;
		this.getUserLibraryUseCase = getUserLibraryUseCase;
		this.getUserBookDetailUseCase = getUserBookDetailUseCase;
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

		var body = new UserBookResponse(
				output.userBookId(),
				output.bookId(),
				output.ownedFormat(),
				output.createdAt()
				);

		return output.created()
				? ResponseEntity.status(HttpStatus.CREATED).body(body)
						: ResponseEntity.ok(body);
	}

	@GetMapping
	public ResponseEntity<List<LibraryItemResponse>> getLibrary(
			@RequestParam(required = false) Shelf shelf,
			Authentication authentication) {

		UUID userId = (UUID) authentication.getPrincipal();

		var input = new GetUserLibraryInput(userId, shelf);
		List<LibraryItemOutput> items = getUserLibraryUseCase.execute(input);

		List<LibraryItemResponse> response = items.stream()
				.map(item -> new LibraryItemResponse(
						item.userBookId(),
						item.bookId(),
						item.title(),
						item.subtitle(),
						item.authors(),
						item.coverImageUrl(),
						item.ownedFormat(),
						item.shelf(),
						item.addedAt()
						))
				.toList();

		return ResponseEntity.ok(response);
	}

	@GetMapping("/{userBookId}")
	public ResponseEntity<UserBookDetailResponse> getBookDetail(
			@PathVariable UUID userBookId,
			Authentication authentication) {

		UUID userId = (UUID) authentication.getPrincipal();

		var input = new GetUserBookDetailInput(userId, userBookId);
		UserBookDetailOutput output = getUserBookDetailUseCase.execute(input);

		List<ReadingInstanceSummaryResponse> instanceResponses = output.readingInstances().stream()
				.map(this::toInstanceSummaryResponse)
				.toList();

		return ResponseEntity.ok(new UserBookDetailResponse(
				output.userBookId(),
				output.bookId(),
				output.title(),
				output.subtitle(),
				output.authors(),
				output.coverImageUrl(),
				output.ownedFormat(),
				output.shelf(),
				output.addedAt(),
				instanceResponses
				));
	}

	private ReadingInstanceSummaryResponse toInstanceSummaryResponse(ReadingInstanceSummary summary) {
		return new ReadingInstanceSummaryResponse(
				summary.id(),
				summary.readNumber(),
				summary.status(),
				summary.currentPage(),
				summary.startDate(),
				summary.endDate(),
				summary.createdAt()
				);
	}

	@PostMapping("/{userBookId}/reading-instances")
	public ResponseEntity<ReadingInstanceResponse> startReadingInstance(
			@PathVariable UUID userBookId,
			@RequestBody StartReadingInstanceRequest request,
			Authentication authentication) {

		UUID userId = (UUID) authentication.getPrincipal();

		var input = new StartReadingInstanceInput(
				userId,
				userBookId,
				request.status(),
				request.currentPage(),
				request.startDate(),
				request.endDate(),
				request.confirmReread()
				);

		var output = startReadingInstanceUseCase.execute(input);

		return ResponseEntity.status(HttpStatus.CREATED).body(new ReadingInstanceResponse(
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
}