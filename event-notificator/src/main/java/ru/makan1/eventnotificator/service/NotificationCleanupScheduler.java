package ru.makan1.eventnotificator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.makan1.eventnotificator.repository.NotificationEventPayloadsRepository;
import ru.makan1.eventnotificator.repository.NotificationsRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class NotificationCleanupScheduler {

    private final NotificationsRepository notificationsRepository;
    private final NotificationEventPayloadsRepository notificationEventPayloadsRepository;

    @Value("${spring.scheduler.retention-days:7}")
    private int retentionDays;

    public NotificationCleanupScheduler(NotificationsRepository notificationsRepository,
                                        NotificationEventPayloadsRepository notificationEventPayloadsRepository
    ) {
        this.notificationsRepository = notificationsRepository;
        this.notificationEventPayloadsRepository = notificationEventPayloadsRepository;
    }

    @Scheduled(fixedRateString = "${spring.scheduler.cleanup-fixed-rate}")
    @Transactional
    public void cleanupOldNotifications() {
        Instant threshold = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        long deleted = notificationsRepository.deleteByCreatedAtBefore(threshold);
        long deletedPayloads = notificationEventPayloadsRepository.deleteOrphanPayloads();
        if (deleted > 0 || deletedPayloads > 0) {
            log.info("Deleted {} notifications and {} notification payloads older than {} days",
                    deleted,
                    deletedPayloads,
                    retentionDays
            );
        }
    }
}

