package ru.makan1.eventnotificator.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

public record NotificationResponse(
        Long notificationId,
        String type,
        Long eventId,
        Instant createdAt,
        boolean isRead,
        String message,
        JsonNode payload
) {
}

