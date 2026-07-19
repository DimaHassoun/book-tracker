package com.booktracker.book_tracker.application.usecase.session;

import com.booktracker.book_tracker.domain.exception.InvalidReadingSessionException;
import com.booktracker.book_tracker.domain.exception.ReadingInstanceNotFoundException;
import com.booktracker.book_tracker.domain.model.ReadingInstance;
import com.booktracker.book_tracker.domain.model.ReadingSession;
import com.booktracker.book_tracker.domain.model.UserBook;
import com.booktracker.book_tracker.domain.repository.ReadingInstanceRepository;
import com.booktracker.book_tracker.domain.repository.ReadingSessionRepository;
import com.booktracker.book_tracker.domain.repository.UserBookRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Logs one block of reading activity against a specific ReadingInstance.
 * Deliberately keyed off reading_instance_id, not user_book_id — a reread
 * has its own instance and its own separate session history, never blended
 * with a prior read's sessions.
 *
 * Sessions and status transitions remain independent decisions (this use
 * case never touches status or status_change_log), but a session DOES
 * advance current_page when it represents further progress than what's
 * already known — mirroring "I read up to page X" as a fact about the
 * instance, not just a historical log entry. This update is monotonic:
 * current_page only ever increases from a session, never decreases,
 * because a session logged out of order (e.g. re-reading an earlier
 * chapter) doesn't mean the reader lost progress. A status transition's
 * explicit page_at_change (handled in ApplyStatusTransitionUseCase) is the
 * one case allowed to move current_page in either direction, since that
 * represents the reader's deliberate, declared position — not a session.
 */
@Service
public class LogReadingSessionUseCase {

    private final ReadingInstanceRepository readingInstanceRepository;
    private final UserBookRepository userBookRepository;
    private final ReadingSessionRepository readingSessionRepository;

    public LogReadingSessionUseCase(ReadingInstanceRepository readingInstanceRepository,
                                     UserBookRepository userBookRepository,
                                     ReadingSessionRepository readingSessionRepository) {
        this.readingInstanceRepository = readingInstanceRepository;
        this.userBookRepository = userBookRepository;
        this.readingSessionRepository = readingSessionRepository;
    }

    public LogReadingSessionOutput execute(LogReadingSessionInput input) {

        if (input.startPage() == null) {
            throw new InvalidReadingSessionException("start_page is required.");
        }
        if (input.startPage() < 0) {
            throw new InvalidReadingSessionException("start_page must be greater than or equal to 0.");
        }
        if (input.endPage() == null) {
            throw new InvalidReadingSessionException("end_page is required.");
        }
        if (input.endPage() <= input.startPage()) {
            throw new InvalidReadingSessionException("end_page must be greater than start_page.");
        }
        if (input.sessionDate() != null && input.sessionDate().isAfter(LocalDate.now())) {
            throw new InvalidReadingSessionException("session_date cannot be in the future.");
        }
        if (input.durationMinutes() != null && input.durationMinutes() <= 0) {
            throw new InvalidReadingSessionException("duration_minutes must be greater than 0 when provided.");
        }

        ReadingInstance instance = readingInstanceRepository.findById(input.readingInstanceId())
                .orElseThrow(() -> new ReadingInstanceNotFoundException(
                        "No reading instance " + input.readingInstanceId() + " found for this user"));

        // Same non-disclosure pattern as UpdateReadingStatusUseCase: a 404 either
        // way, whether the instance doesn't exist or belongs to someone else.
        UserBook userBook = userBookRepository.findById(instance.getUserBookId())
                .filter(ub -> ub.getUserId().equals(input.userId()))
                .orElseThrow(() -> new ReadingInstanceNotFoundException(
                        "No reading instance " + input.readingInstanceId() + " found for this user"));

        ReadingSession session = new ReadingSession();
        session.setReadingInstanceId(instance.getId());
        session.setSessionDate(input.sessionDate() != null ? input.sessionDate() : LocalDate.now());
        session.setStartPage(input.startPage());
        session.setEndPage(input.endPage());
        session.setDurationMinutes(input.durationMinutes());

        ReadingSession saved = readingSessionRepository.save(session);

        advanceCurrentPageIfNeeded(instance, saved.getEndPage());

        return toOutput(saved);
    }

    private void advanceCurrentPageIfNeeded(ReadingInstance instance, int sessionEndPage) {
        Integer currentPage = instance.getCurrentPage();
        if (currentPage == null || sessionEndPage > currentPage) {
            instance.setCurrentPage(sessionEndPage);
            readingInstanceRepository.save(instance);
        }
        // Otherwise: session ended at or before current_page — never decrease it.
    }

    private LogReadingSessionOutput toOutput(ReadingSession session) {
        return new LogReadingSessionOutput(
                session.getId(),
                session.getReadingInstanceId(),
                session.getSessionDate(),
                session.getStartPage(),
                session.getEndPage(),
                session.getDurationMinutes(),
                session.pagesRead(),
                session.getCreatedAt()
        );
    }
}