package com.booktracker.book_tracker.application.usecase.book;

import com.booktracker.book_tracker.domain.model.Book;
import com.booktracker.book_tracker.domain.repository.BookRepository;
import com.booktracker.book_tracker.domain.valueobject.ExternalSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookResolverTest {

    @Mock
    private BookRepository bookRepository;

    private BookResolver bookResolver;

    @BeforeEach
    void setUp() {
        bookResolver = new BookResolver(bookRepository);
    }

    private BookMetadata sampleMetadata() {
        return new BookMetadata(
                ExternalSource.GOOGLE_BOOKS,
                "gb-123",
                "9780000000001",
                "0000000001",
                "Dune",
                null,
                List.of("Frank Herbert"),
                "Chilton Books",
                null,
                "A desert planet epic.",
                412,
                List.of("Science Fiction"),
                "http://example.com/dune.jpg",
                "en"
        );
    }

    @Test
    void resolveOrCreate_whenBookExists_shouldReturnExistingBookWithoutSaving() {
        Book existing = new Book();
        existing.setId(UUID.randomUUID());
        existing.setExternalSource(ExternalSource.GOOGLE_BOOKS);
        existing.setExternalId("gb-123");

        when(bookRepository.findByExternalSourceAndExternalId(ExternalSource.GOOGLE_BOOKS, "gb-123"))
                .thenReturn(Optional.of(existing));

        Book result = bookResolver.resolveOrCreate(sampleMetadata());

        assertThat(result).isSameAs(existing);
        verify(bookRepository, never()).save(any());
    }

    @Test
    void resolveOrCreate_whenBookDoesNotExist_shouldMapMetadataAndSaveNewBook() {
        when(bookRepository.findByExternalSourceAndExternalId(ExternalSource.GOOGLE_BOOKS, "gb-123"))
                .thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        Book result = bookResolver.resolveOrCreate(sampleMetadata());

        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        Book saved = captor.getValue();

        assertThat(saved.getTitle()).isEqualTo("Dune");
        assertThat(saved.getExternalSource()).isEqualTo(ExternalSource.GOOGLE_BOOKS);
        assertThat(saved.getExternalId()).isEqualTo("gb-123");
        assertThat(saved.getIsbn13()).isEqualTo("9780000000001");
        assertThat(saved.getAuthors()).containsExactly("Frank Herbert");
        assertThat(saved.getPageCount()).isEqualTo(412);
        assertThat(result).isSameAs(saved);
    }
}
