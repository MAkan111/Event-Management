package ru.makan1.eventmanagement.event.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventResponse(
        Long id,
        Long locationId,
        Long ownerId,
        String name,
        LocalDateTime date,
        int occupiedPlaces,
        int duration,
        BigDecimal cost,
        int maxPlaces,
        String status
) {
}
