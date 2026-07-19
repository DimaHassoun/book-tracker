package com.booktracker.book_tracker.infrastructure.persistence.mapper;

import com.booktracker.book_tracker.domain.model.ReadingSession;
import com.booktracker.book_tracker.infrastructure.persistence.entity.ReadingSessionEntity;
import org.springframework.stereotype.Component;

@Component
public class ReadingSessionEntityMapper {

    public ReadingSession toDomain(ReadingSessionEntity entity) {
        if (entity == null) return null;
        ReadingSession session = new ReadingSession();
        session.setId(entity.getId());
        session.setReadingInstanceId(entity.getReadingInstanceId());
        session.setSessionDate(entity.getSessionDate());
        session.setStartPage(entity.getStartPage());
        session.setEndPage(entity.getEndPage());
        session.setDurationMinutes(entity.getDurationMinutes());
        session.setCreatedAt(entity.getCreatedAt());
        return session;
    }

    public ReadingSessionEntity toEntity(ReadingSession session) {
        if (session == null) return null;
        ReadingSessionEntity entity = new ReadingSessionEntity();
        entity.setId(session.getId());
        entity.setReadingInstanceId(session.getReadingInstanceId());
        entity.setSessionDate(session.getSessionDate());
        entity.setStartPage(session.getStartPage());
        entity.setEndPage(session.getEndPage());
        entity.setDurationMinutes(session.getDurationMinutes());
        entity.setCreatedAt(session.getCreatedAt());
        return entity;
    }
}