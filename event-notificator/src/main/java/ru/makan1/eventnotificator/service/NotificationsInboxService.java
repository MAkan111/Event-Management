package ru.makan1.eventnotificator.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.makan1.eventnotificator.dto.NotificationResponse;
import ru.makan1.eventnotificator.entity.NotificationEventPayloads;
import ru.makan1.eventnotificator.entity.Notifications;
import ru.makan1.eventnotificator.repository.NotificationEventPayloadsRepository;
import ru.makan1.eventnotificator.repository.NotificationsRepository;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class NotificationsInboxService {

    private final NotificationsRepository notificationsRepository;
    private final NotificationEventPayloadsRepository payloadsRepository;

    public NotificationsInboxService(
            NotificationsRepository notificationsRepository,
            NotificationEventPayloadsRepository payloadsRepository
    ) {
        this.notificationsRepository = notificationsRepository;
        this.payloadsRepository = payloadsRepository;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        List<Notifications> notifications = notificationsRepository.findAllByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        if (notifications.isEmpty()) {
            return List.of();
        }

        List<Long> payloadIds = notifications.stream().map(Notifications::getPayloadId).distinct().toList();
        Map<Long, NotificationEventPayloads> payloadById = payloadsRepository.findAllByPayloadIdIn(payloadIds).stream()
                .collect(java.util.stream.Collectors.toMap(NotificationEventPayloads::getPayloadId, Function.identity()));

        return notifications.stream()
                .map(n -> {
                    NotificationEventPayloads payloadEntity = payloadById.get(n.getPayloadId());
                    String type = payloadEntity != null ? payloadEntity.getEventType() : "UNKNOWN";
                    Long eventId = payloadEntity != null ? payloadEntity.getEventId() : null;
                    JsonNode payload = payloadEntity != null ? payloadEntity.getPayload() : null;
                    String message = buildMessage(type, eventId, payload);
                    return new NotificationResponse(
                            n.getNotificationId(),
                            type,
                            eventId,
                            n.getCreatedAt(),
                            Boolean.TRUE.equals(n.getIsRead()),
                            message,
                            payload
                    );
                })
                .toList();
    }

    @Transactional
    public void markAsRead(Long userId, Collection<Long> notificationIds) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return;
        }
        notificationsRepository.markAsRead(userId, notificationIds, Instant.now());
    }

    private static String buildMessage(String type, Long eventId, JsonNode payload) {
        if (eventId == null) {
            return "Изменение мероприятия";
        }
        if (type == null) {
            return "Изменение мероприятия #" + eventId;
        }
        return switch (type) {
            case "EVENT_CREATED" -> "Создано мероприятие #" + eventId;
            case "EVENT_CANCELED" -> "Отменено мероприятие #" + eventId;
            case "EVENT_UPDATED" -> "Обновлено мероприятие #" + eventId;
            default -> "Изменение мероприятия #" + eventId;
        };
    }
}

