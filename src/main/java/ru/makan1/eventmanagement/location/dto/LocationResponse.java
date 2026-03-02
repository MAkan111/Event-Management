package ru.makan1.eventmanagement.location.dto;

import jakarta.validation.constraints.Min;

public record LocationResponse(
        Long id,
        String name,
        String address,
        @Min(5)
        int capacity,
        String description
) {
}
