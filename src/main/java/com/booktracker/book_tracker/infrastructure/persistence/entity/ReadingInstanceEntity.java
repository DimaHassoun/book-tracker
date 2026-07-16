package com.booktracker.book_tracker.infrastructure.persistence.entity;

import com.booktracker.book_tracker.domain.valueobject.ReadingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "reading_instances")
@Getter
@Setter
@NoArgsConstructor
public class ReadingInstanceEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_book_id", nullable = false)
    private UUID userBookId;

    @Column(name = "read_number", nullable = false)
    private int readNumber;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false)
    private ReadingStatus status;

    @Column(name = "current_page")
    private Integer currentPage;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}