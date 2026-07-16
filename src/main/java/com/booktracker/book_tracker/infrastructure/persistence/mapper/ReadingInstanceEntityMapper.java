package com.booktracker.book_tracker.infrastructure.persistence.mapper;

import com.booktracker.book_tracker.domain.model.ReadingInstance;
import com.booktracker.book_tracker.infrastructure.persistence.entity.ReadingInstanceEntity;
import org.springframework.stereotype.Component;

@Component
public class ReadingInstanceEntityMapper {

    public ReadingInstance toDomain(ReadingInstanceEntity entity) {
        if (entity == null) return null;
        ReadingInstance instance = new ReadingInstance();
        instance.setId(entity.getId());
        instance.setUserBookId(entity.getUserBookId());
        instance.setReadNumber(entity.getReadNumber());
        instance.setStatus(entity.getStatus());
        instance.setCurrentPage(entity.getCurrentPage());
        instance.setStartDate(entity.getStartDate());
        instance.setEndDate(entity.getEndDate());
        instance.setCreatedAt(entity.getCreatedAt());
        instance.setUpdatedAt(entity.getUpdatedAt());
        return instance;
    }

    public ReadingInstanceEntity toEntity(ReadingInstance instance) {
        if (instance == null) return null;
        ReadingInstanceEntity entity = new ReadingInstanceEntity();
        entity.setId(instance.getId());
        entity.setUserBookId(instance.getUserBookId());
        entity.setReadNumber(instance.getReadNumber());
        entity.setStatus(instance.getStatus());
        entity.setCurrentPage(instance.getCurrentPage());
        entity.setStartDate(instance.getStartDate());
        entity.setEndDate(instance.getEndDate());
        entity.setCreatedAt(instance.getCreatedAt());
        entity.setUpdatedAt(instance.getUpdatedAt());
        return entity;
    }
}