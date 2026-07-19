package com.booktracker.book_tracker.domain.model;
import com.booktracker.book_tracker.domain.valueobject.ExternalSource;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Canonical, catalog-level book data — shared across every user who has
 * added this book, never duplicated per user. A {@code Book} always
 * originates from an external source ({@link com.booktracker.book_tracker.domain.valueobject.ExternalSource})
 * and is resolved/created idempotently by
 * {@link com.booktracker.book_tracker.application.usecase.book.BookResolver}
 * on first reference — never hand-constructed with arbitrary user-supplied
 * metadata outside that path.
 */

public class Book {

    private UUID id;
    private String isbn13;
    private String isbn10;
    private String title;
    private String subtitle;
    private List<String> authors;
    private String publisher;
    private LocalDate publishedDate;
    private String description;
    private Integer pageCount;
    private List<String> genres;
    private String coverImageUrl;
    private ExternalSource externalSource;
    private String externalId;
    private String language;
    private Instant createdAt;

    public Book() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getIsbn13() {
        return isbn13;
    }

    public void setIsbn13(String isbn13) {
        this.isbn13 = isbn13;
    }

    public String getIsbn10() {
        return isbn10;
    }

    public void setIsbn10(String isbn10) {
        this.isbn10 = isbn10;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public void setAuthors(List<String> authors) {
        this.authors = authors;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public ExternalSource getExternalSource() {
        return externalSource;
    }

    public void setExternalSource(ExternalSource externalSource) {
        this.externalSource = externalSource;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}