package com.booktracker.book_tracker.infrastructure.persistence.entity;
import com.booktracker.book_tracker.domain.valueobject.ExternalSource;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
public class BookEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "isbn_13", length = 13)
    private String isbn13;

    @Column(name = "isbn_10", length = 10)
    private String isbn10;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 500)
    private String subtitle;

    @Column(columnDefinition = "text[]")
    private List<String> authors;

    @Column(length = 255)
    private String publisher;

    @Column(name = "published_date")
    private LocalDate publishedDate;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(columnDefinition = "text[]")
    private List<String> genres;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "book_external_source", nullable = false)
    private ExternalSource externalSource;

    @Column(name = "external_id", nullable = false, length = 50)
    private String externalId;

    @Column(length = 10)
    private String language = "en";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}