package com.booktracker.book_tracker.infrastructure.persistence.mapper;

import com.booktracker.book_tracker.domain.model.User;
import com.booktracker.book_tracker.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserEntityMapper {

    public User toDomain(UserEntity entity) {
        if (entity == null) return null;
        return new User(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getBio(),
                entity.getAvatarUrl(),
                entity.isPrivate(),
                entity.getTimezone(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public UserEntity toEntity(User user) {
        if (user == null) return null;
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setBio(user.getBio());
        entity.setAvatarUrl(user.getAvatarUrl());
        entity.setPrivate(user.isPrivate());
        entity.setTimezone(user.getTimezone());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        return entity;
    }
}