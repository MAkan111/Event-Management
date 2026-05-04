package ru.makan1.eventnotificator.service;

import org.springframework.stereotype.Service;
import ru.makan1.eventcommon.messages.EventKafkaMessage;
import ru.makan1.eventnotificator.entity.Notifications;
import ru.makan1.eventnotificator.repository.NotificationsRepository;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationsService {

    private final NotificationsRepository notificationsRepository;

    public NotificationsService(NotificationsRepository notificationsRepository,
                                NotificationEventPayloadsService notificationEventPayloadsService
    ) {
        this.notificationsRepository = notificationsRepository;
        this.notificationEventPayloadsService = notificationEventPayloadsService;
    }

    private final NotificationEventPayloadsService notificationEventPayloadsService;

    public void processMessage(EventKafkaMessage message) {
        Long payloadId = notificationEventPayloadsService.getOrCreatePayloadId(message);
        List<Long> subscribers = message.getSubscribers() != null ? message.getSubscribers() : List.of();

        for (Long userId : subscribers) {
            if (userId == null) {
                continue;
            }
            if (notificationsRepository.existsByUserIdAndPayloadId(userId, payloadId)) {
                continue;
            }

            Notifications notification = new Notifications();
            notification.setUserId(userId);
            notification.setPayloadId(payloadId);
            notification.setIsRead(false);
            notification.setCreatedAt(Instant.now());
            notification.setReadAt(null);

            notificationsRepository.save(notification);
        }
    }
}
