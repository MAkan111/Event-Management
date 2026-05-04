package ru.makan1.eventmanager.location.dto;

import java.time.LocalDateTime;

public record ErrorMessageResponse (
        String message,
        String detailedMessage,
        LocalDateTime dateTime
) {
}
