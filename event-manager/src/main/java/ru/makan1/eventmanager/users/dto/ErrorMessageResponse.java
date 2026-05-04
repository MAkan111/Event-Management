package ru.makan1.eventmanager.users.dto;

import java.time.LocalDateTime;

public record ErrorMessageResponse (
        String message,
        String detailedMessage,
        LocalDateTime dateTime
) {
}
