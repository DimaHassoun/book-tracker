package com.booktracker.book_tracker.application.usecase.book;

import com.booktracker.book_tracker.domain.model.Book;
import com.booktracker.book_tracker.infrastructure.external.googlebooks.GoogleBooksClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchBooksUseCase {

    private final GoogleBooksClient googleBooksClient;

    public SearchBooksUseCase(GoogleBooksClient googleBooksClient) {
        this.googleBooksClient = googleBooksClient;
    }

    public List<Book> execute(String query, int maxResults) {
        return googleBooksClient.search(query, maxResults);
    }
}