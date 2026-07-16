package com.booktracker.book_tracker.infrastructure.persistence.mapper;

import com.booktracker.book_tracker.domain.model.UserBook;
import com.booktracker.book_tracker.infrastructure.persistence.entity.UserBookEntity;
import org.springframework.stereotype.Component;

@Component
public class UserBookEntityMapper {

    public UserBook toDomain(UserBookEntity entity) {
        if (entity == null) return null;
        UserBook userBook = new UserBook();
        userBook.setId(entity.getId());
        userBook.setUserId(entity.getUserId());
        userBook.setBookId(entity.getBookId());
        userBook.setOwnedFormat(entity.getOwnedFormat());
        userBook.setCreatedAt(entity.getCreatedAt());
        userBook.setUpdatedAt(entity.getUpdatedAt());
        return userBook;
    }

    public UserBookEntity toEntity(UserBook userBook) {
        if (userBook == null) return null;
        UserBookEntity entity = new UserBookEntity();
        entity.setId(userBook.getId());
        entity.setUserId(userBook.getUserId());
        entity.setBookId(userBook.getBookId());
        entity.setOwnedFormat(userBook.getOwnedFormat());
        entity.setCreatedAt(userBook.getCreatedAt());
        entity.setUpdatedAt(userBook.getUpdatedAt());
        return entity;
    }
}