package com.booktracker.book_tracker.domain.model;

import com.booktracker.book_tracker.domain.valueobject.BookFormat;

import java.time.Instant;
import java.util.UUID;

/**
 * A user's library membership record for a single book — "this book is on
 * my shelf." One row ever, per (user, book) pair, regardless of how many
 * times the book is read.
 * 
 * Deliberately holds no reading-progress state (no status, no dates, no
 * rating). A book with a {@code UserBook} row but zero associated
 * {@link ReadingInstance} rows is exactly what "Want to Read" means in this
 * system — there is no separate status field for it, and none should be
 * added here; that would duplicate a fact this table's mere existence
 * already represents.
 */

public class UserBook {

    private UUID id;
    private UUID userId;
    private UUID bookId;
    private BookFormat ownedFormat;
    private Instant createdAt;
    private Instant updatedAt;

    public UserBook() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public UUID getBookId() { return bookId; }
    public void setBookId(UUID bookId) { this.bookId = bookId; }

    public BookFormat getOwnedFormat() { return ownedFormat; }
    public void setOwnedFormat(BookFormat ownedFormat) { this.ownedFormat = ownedFormat; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}