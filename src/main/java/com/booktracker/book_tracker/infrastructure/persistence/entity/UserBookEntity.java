package com.booktracker.book_tracker.infrastructure.persistence.entity;

import com.booktracker.book_tracker.domain.valueobject.BookFormat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "user_books")
@Getter
@Setter
@NoArgsConstructor
public class UserBookEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "book_id", nullable = false)
    private UUID bookId;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "owned_format", nullable = false)
    private BookFormat ownedFormat;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}