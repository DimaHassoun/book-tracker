package com.booktracker.book_tracker.infrastructure.persistence.entity;

import com.booktracker.book_tracker.domain.valueobject.ReadingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "status_change_log")
@Getter
@Setter
@NoArgsConstructor
public class StatusChangeLogEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "reading_instance_id", nullable = false)
    private UUID readingInstanceId;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "old_status")
    private ReadingStatus oldStatus;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "new_status", nullable = false)
    private ReadingStatus newStatus;

    @Column(name = "page_at_change")
    private Integer pageAtChange;

    @Column(name = "changed_at", nullable = false, updatable = false)
    private Instant changedAt;
}