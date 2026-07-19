package com.booktracker.book_tracker.infrastructure.persistence.mapper;

import com.booktracker.book_tracker.domain.model.StatusChangeLog;
import com.booktracker.book_tracker.infrastructure.persistence.entity.StatusChangeLogEntity;
import org.springframework.stereotype.Component;

@Component
public class StatusChangeLogEntityMapper {

    public StatusChangeLog toDomain(StatusChangeLogEntity entity) {
        if (entity == null) return null;
        StatusChangeLog log = new StatusChangeLog();
        log.setId(entity.getId());
        log.setReadingInstanceId(entity.getReadingInstanceId());
        log.setOldStatus(entity.getOldStatus());
        log.setNewStatus(entity.getNewStatus());
        log.setPageAtChange(entity.getPageAtChange());
        log.setChangedAt(entity.getChangedAt());
        return log;
    }

    public StatusChangeLogEntity toEntity(StatusChangeLog log) {
        if (log == null) return null;
        StatusChangeLogEntity entity = new StatusChangeLogEntity();
        entity.setId(log.getId());
        entity.setReadingInstanceId(log.getReadingInstanceId());
        entity.setOldStatus(log.getOldStatus());
        entity.setNewStatus(log.getNewStatus());
        entity.setPageAtChange(log.getPageAtChange());
        entity.setChangedAt(log.getChangedAt());
        return entity;
    }
}