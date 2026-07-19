package com.booktracker.book_tracker.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "reading_sessions")
@Getter
@Setter
@NoArgsConstructor
public class ReadingSessionEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "reading_instance_id", nullable = false)
    private UUID readingInstanceId;

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "start_page", nullable = false)
    private int startPage;

    @Column(name = "end_page", nullable = false)
    private int endPage;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}