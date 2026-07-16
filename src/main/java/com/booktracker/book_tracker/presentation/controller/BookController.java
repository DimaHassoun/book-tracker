package com.booktracker.book_tracker.presentation.controller;

import com.booktracker.book_tracker.application.usecase.book.SearchBooksUseCase;
import com.booktracker.book_tracker.domain.model.Book;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final SearchBooksUseCase searchBooksUseCase;

    public BookController(SearchBooksUseCase searchBooksUseCase) {
        this.searchBooksUseCase = searchBooksUseCase;
    }

    @GetMapping("/search")
    public List<Book> search(@RequestParam String q, @RequestParam(defaultValue = "10") int size) {
        return searchBooksUseCase.execute(q, size);
    }
}