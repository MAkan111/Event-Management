package ru.makan1.eventmanagement.location.dto;

import java.time.LocalDateTime;

public record ErrorMessageResponse (
        String message,
        String detailedMessage,
        LocalDateTime dateTime
) {
}
