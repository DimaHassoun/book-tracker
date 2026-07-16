package com.booktracker.book_tracker.domain.model;

import com.booktracker.book_tracker.domain.valueobject.ReadingStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

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