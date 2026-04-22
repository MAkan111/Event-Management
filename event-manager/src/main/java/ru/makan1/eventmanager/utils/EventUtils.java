package ru.makan1.eventmanager.utils;

import ru.makan1.eventmanager.event.enums.EventStatus;

public class EventUtils {
    public static String normalizeBlankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static EventStatus parseEventStatusOrNull(String value) {
        String normalized = normalizeBlankToNull(value);
        if (normalized == null) {
            return null;
        }
        return EventStatus.valueOf(normalized);
    }
}
