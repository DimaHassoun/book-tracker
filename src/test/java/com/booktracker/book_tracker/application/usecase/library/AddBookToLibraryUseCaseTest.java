package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.application.usecase.book.BookMetadata;
import com.booktracker.book_tracker.application.usecase.book.BookResolver;
import com.booktracker.book_tracker.domain.model.Book;
import com.booktracker.book_tracker.domain.model.UserBook;
import com.booktracker.book_tracker.domain.repository.UserBookRepository;
import com.booktracker.book_tracker.domain.valueobject.BookFormat;
import com.booktracker.book_tracker.domain.valueobject.ExternalSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddBookToLibraryUseCaseTest {

    @Mock
    private BookResolver bookResolver;

    @Mock
    private UserBookRepository userBookRepository;

    private AddBookToLibraryUseCase useCase;

    private UUID userId;
    private Book resolvedBook;

    @BeforeEach
    void setUp() {
        useCase = new AddBookToLibraryUseCase(bookResolver, userBookRepository);
        userId = UUID.randomUUID();
        resolvedBook = new Book();
        resolvedBook.setId(UUID.randomUUID());
        resolvedBook.setTitle("Dune");

        when(bookResolver.resolveOrCreate(any(BookMetadata.class))).thenReturn(resolvedBook);
    }

    private AddBookToLibraryInput sampleInput() {
        return new AddBookToLibraryInput(
                userId,
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
                "en",
                BookFormat.PHYSICAL
        );
    }

    @Test
    void execute_whenBookNotInLibrary_shouldCreateNewUserBookAndReturnCreatedTrue() {
        when(userBookRepository.findByUserIdAndBookId(userId, resolvedBook.getId())).thenReturn(Optional.empty());

        UserBook saved = new UserBook();
        saved.setId(UUID.randomUUID());
        saved.setUserId(userId);
        saved.setBookId(resolvedBook.getId());
        saved.setOwnedFormat(BookFormat.PHYSICAL);
        saved.setCreatedAt(Instant.now());
        when(userBookRepository.save(any(UserBook.class))).thenReturn(saved);

        AddBookToLibraryOutput output = useCase.execute(sampleInput());

        assertThat(output.created()).isTrue();
        assertThat(output.userBookId()).isEqualTo(saved.getId());
        assertThat(output.bookId()).isEqualTo(resolvedBook.getId());
        assertThat(output.ownedFormat()).isEqualTo(BookFormat.PHYSICAL);

        ArgumentCaptor<UserBook> captor = ArgumentCaptor.forClass(UserBook.class);
        verify(userBookRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(userId);
        assertThat(captor.getValue().getBookId()).isEqualTo(resolvedBook.getId());
    }

    @Test
    void execute_whenBookAlreadyInLibrary_shouldReturnExistingAssociationAndCreatedFalse() {
        UserBook existing = new UserBook();
        existing.setId(UUID.randomUUID());
        existing.setUserId(userId);
        existing.setBookId(resolvedBook.getId());
        existing.setOwnedFormat(BookFormat.EBOOK);
        existing.setCreatedAt(Instant.now());

        when(userBookRepository.findByUserIdAndBookId(userId, resolvedBook.getId())).thenReturn(Optional.of(existing));

        AddBookToLibraryOutput output = useCase.execute(sampleInput());

        assertThat(output.created()).isFalse();
        assertThat(output.userBookId()).isEqualTo(existing.getId());
        assertThat(output.ownedFormat()).isEqualTo(BookFormat.EBOOK);
        verify(userBookRepository, never()).save(any());
    }

    @Test
    void execute_shouldPassRequestFieldsToBookResolverAsMetadata() {
        when(userBookRepository.findByUserIdAndBookId(any(), any())).thenReturn(Optional.empty());
        when(userBookRepository.save(any(UserBook.class))).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(sampleInput());

        ArgumentCaptor<BookMetadata> captor = ArgumentCaptor.forClass(BookMetadata.class);
        verify(bookResolver).resolveOrCreate(captor.capture());
        BookMetadata metadata = captor.getValue();

        assertThat(metadata.externalSource()).isEqualTo(ExternalSource.GOOGLE_BOOKS);
        assertThat(metadata.externalId()).isEqualTo("gb-123");
        assertThat(metadata.title()).isEqualTo("Dune");
        assertThat(metadata.authors()).containsExactly("Frank Herbert");
    }

    @Test
    void execute_whenExistingAssociationHasNullCreatedAt_shouldFallBackToNow() {
        UserBook existing = new UserBook();
        existing.setId(UUID.randomUUID());
        existing.setUserId(userId);
        existing.setBookId(resolvedBook.getId());
        existing.setOwnedFormat(BookFormat.EBOOK);
        existing.setCreatedAt(null);

        when(userBookRepository.findByUserIdAndBookId(userId, resolvedBook.getId())).thenReturn(Optional.of(existing));

        AddBookToLibraryOutput output = useCase.execute(sampleInput());

        assertThat(output.createdAt()).isNotNull();
    }
}
