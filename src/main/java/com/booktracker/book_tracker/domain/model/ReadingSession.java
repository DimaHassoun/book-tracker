package com.booktracker.book_tracker.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A single logged block of reading activity within one specific
 * {@link ReadingInstance}. Always scoped to reading_instance_id, never to
 * user_book_id directly — a reread has its own ReadingInstance and
 * therefore its own, separate set of sessions.
 */
public class ReadingSession {

    private UUID id;
    private UUID readingInstanceId;
    private LocalDate sessionDate;
    private int startPage;
    private int endPage;
    private Integer durationMinutes; // nullable 
    private Instant createdAt;

    public ReadingSession() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getReadingInstanceId() { return readingInstanceId; }
    public void setReadingInstanceId(UUID readingInstanceId) { this.readingInstanceId = readingInstanceId; }

    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }

    public int getStartPage() { return startPage; }
    public void setStartPage(int startPage) { this.startPage = startPage; }

    public int getEndPage() { return endPage; }
    public void setEndPage(int endPage) { this.endPage = endPage; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public int pagesRead() {
        return endPage - startPage;
    }
}