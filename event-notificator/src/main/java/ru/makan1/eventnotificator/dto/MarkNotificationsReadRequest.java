package ru.makan1.eventnotificator.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MarkNotificationsReadRequest(
        @NotNull List<Long> notificationIds
) {
}

