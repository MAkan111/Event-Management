package ru.makan1.eventnotificator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.makan1.eventnotificator.repository.NotificationsRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
public class NotificationCleanupScheduler {

    private final NotificationsRepository notificationsRepository;

    @Value("${spring.scheduler.retention-days:7}")
    private int retentionDays;

    public NotificationCleanupScheduler(NotificationsRepository notificationsRepository) {
        this.notificationsRepository = notificationsRepository;
    }

    @Scheduled(fixedRateString = "${spring.scheduler.cleanup-fixed-rate}")
    public void cleanupOldNotifications() {
        Instant threshold = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        long deleted = notificationsRepository.deleteByCreatedAtBefore(threshold);
        if (deleted > 0) {
            log.info("Deleted {} notifications older than {} days", deleted, retentionDays);
        }
    }
}

