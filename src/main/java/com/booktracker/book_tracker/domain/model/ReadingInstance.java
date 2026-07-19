package com.booktracker.book_tracker.domain.model;

import com.booktracker.book_tracker.domain.valueobject.ReadingStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A single reading attempt of a book — one row per read, including
 * rereads. {@code readNumber} is 1 for the first attempt, 2 for the first
 * reread, and so on, assigned as {@code MAX(readNumber) + 1} for the parent
 * {@link UserBook} at creation time.
 * 
 * Every field here follows a strict "don't invent data" rule:
 * 
 *   - {@code currentPage} is never auto-defaulted to 0 — null means the
 *       user didn't report a page, not that they're at the start.</li>
 *   - {@code startDate} is only auto-populated when the initiating action
 *       is {@code READING} (starting to read is inherently "as of now");
 *       for {@code READ}, {@code DNF}, or {@code PAUSED} as a first action,
 *       it stays null unless the user explicitly supplies it — those
 *       actions don't logically imply a start moment.</li>
 *   - {@code endDate} is only auto-populated for {@code READ}/{@code DNF},
 *       which do logically imply completion.
 * 
 * See {@link com.booktracker.book_tracker.application.usecase.library.StartReadingInstanceUseCase}
 * for where these rules are actually applied.
 */

public class ReadingInstance {

    private UUID id;
    private UUID userBookId;
    private int readNumber;
    private ReadingStatus status;
    private Integer currentPage;   // nullable — never auto-defaulted
    private LocalDate startDate;   // nullable — only auto-set for READING
    private LocalDate endDate;     // nullable — only auto-set for READ/DNF
    private Instant createdAt;
    private Instant updatedAt;

    public ReadingInstance() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserBookId() { return userBookId; }
    public void setUserBookId(UUID userBookId) { this.userBookId = userBookId; }

    public int getReadNumber() { return readNumber; }
    public void setReadNumber(int readNumber) { this.readNumber = readNumber; }

    public ReadingStatus getStatus() { return status; }
    public void setStatus(ReadingStatus status) { this.status = status; }

    public Integer getCurrentPage() { return currentPage; }
    public void setCurrentPage(Integer currentPage) { this.currentPage = currentPage; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}