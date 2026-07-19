package com.booktracker.book_tracker.application.usecase.library;

import com.booktracker.book_tracker.domain.model.ReadingInstance;
import com.booktracker.book_tracker.domain.model.StatusChangeLog;
import com.booktracker.book_tracker.domain.repository.ReadingInstanceRepository;
import com.booktracker.book_tracker.domain.repository.StatusChangeLogRepository;
import com.booktracker.book_tracker.domain.valueobject.ReadingStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Applies a status change to an EXISTING reading instance: updates the
 * status, writes a status_change_log entry, and applies start/end date
 * side effects. Does not decide whether an update-in-place vs. a new
 * instance is the right action — that decision (resume vs. correction vs.
 * reread) belongs entirely to StartReadingInstanceUseCase. Callers here
 * have already determined an in-place update is correct.
 *
 * A same-status call is a no-op: no log entry, no date changes, instance
 * returned unchanged. (Callers should generally short-circuit this
 * themselves, but it's handled defensively here too.)
 */
@Service
public class ApplyStatusTransitionUseCase {

    private final ReadingInstanceRepository readingInstanceRepository;
    private final StatusChangeLogRepository statusChangeLogRepository;

    public ApplyStatusTransitionUseCase(ReadingInstanceRepository readingInstanceRepository,
                                         StatusChangeLogRepository statusChangeLogRepository) {
        this.readingInstanceRepository = readingInstanceRepository;
        this.statusChangeLogRepository = statusChangeLogRepository;
    }

    public ReadingInstance execute(ReadingInstance instance, ReadingStatus newStatus,
                                    Integer currentPage, LocalDate explicitStartDate, LocalDate explicitEndDate) {

        if (instance.getStatus() == newStatus) {
            return instance;
        }

        ReadingStatus oldStatus = instance.getStatus();
        Integer pageAtChange = currentPage != null ? currentPage : instance.getCurrentPage();

        instance.setStatus(newStatus);
        if (currentPage != null) {
            instance.setCurrentPage(currentPage);
        }

        if (explicitStartDate != null) {
            instance.setStartDate(explicitStartDate);
        } else if (instance.getStartDate() == null && newStatus == ReadingStatus.READING) {
            instance.setStartDate(LocalDate.now());
        }

        if (explicitEndDate != null) {
            instance.setEndDate(explicitEndDate);
        } else if (instance.getEndDate() == null
                && (newStatus == ReadingStatus.READ || newStatus == ReadingStatus.DNF)) {
            instance.setEndDate(LocalDate.now());
        }

        ReadingInstance saved = readingInstanceRepository.save(instance);

        StatusChangeLog log = new StatusChangeLog();
        log.setReadingInstanceId(saved.getId());
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setPageAtChange(pageAtChange);
        statusChangeLogRepository.save(log);

        return saved;
    }
}