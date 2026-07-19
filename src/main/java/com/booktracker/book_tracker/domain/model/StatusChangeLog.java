package com.booktracker.book_tracker.domain.model;

import com.booktracker.book_tracker.domain.valueobject.ReadingStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * An immutable, append-only record of a single status transition on a
 * {@link ReadingInstance} — old status, new status, and the page the
 * change happened at, if known.
 * 
 * {@code oldStatus} is nullable by design: it's null only for the very
 * first log entry tied to a newly created reading instance (there is no
 * "previous" status to record). Same-status transitions (e.g.
 * {@code PAUSED -> PAUSED}) are treated as no-ops upstream and never reach
 * this table at all — every row here represents a real change.
 */

public class StatusChangeLog {

    private UUID id;
    private UUID readingInstanceId;
    private ReadingStatus oldStatus; // nullable — null only if ever logged for instance creation
    private ReadingStatus newStatus;
    private Integer pageAtChange;
    private Instant changedAt;

    public StatusChangeLog() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getReadingInstanceId() { return readingInstanceId; }
    public void setReadingInstanceId(UUID readingInstanceId) { this.readingInstanceId = readingInstanceId; }

    public ReadingStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(ReadingStatus oldStatus) { this.oldStatus = oldStatus; }

    public ReadingStatus getNewStatus() { return newStatus; }
    public void setNewStatus(ReadingStatus newStatus) { this.newStatus = newStatus; }

    public Integer getPageAtChange() { return pageAtChange; }
    public void setPageAtChange(Integer pageAtChange) { this.pageAtChange = pageAtChange; }

    public Instant getChangedAt() { return changedAt; }
    public void setChangedAt(Instant changedAt) { this.changedAt = changedAt; }
}