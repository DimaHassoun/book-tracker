package com.booktracker.book_tracker.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * A registered reader. Plain domain object — no JPA annotations, no
 * framework dependencies, per the Clean Architecture dependency rule.
 * Persistence concerns (column names, table mapping) live entirely in
 * {@link com.booktracker.book_tracker.infrastructure.persistence.entity.UserEntity}
 * and its mapper.
 */

public class User {

    private UUID id;
    private String username;
    private String email;
    private String passwordHash;
    private String bio;
    private String avatarUrl;
    private boolean isPrivate;
    private String timezone;
    private Instant createdAt;
    private Instant updatedAt;

    public User() {
    }

    public User(UUID id, String username, String email, String passwordHash,
                String bio, String avatarUrl, boolean isPrivate, String timezone,
                Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
        this.isPrivate = isPrivate;
        this.timezone = timezone;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}