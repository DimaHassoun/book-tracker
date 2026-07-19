package com.booktracker.book_tracker.domain.repository;

import com.booktracker.book_tracker.domain.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Persistence contract for {@link com.booktracker.book_tracker.domain.model.User}.
 * Domain-layer interface only — no Spring Data, no JPA types leak in here;
 * the JPA-backed implementation lives in the infrastructure layer.
 */

public interface UserRepository {
    User save(User user);
    Optional<User> findById(UUID id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}