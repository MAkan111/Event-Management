package ru.makan1.eventmanager.location.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record LocationRequest(
        @NotNull
        String name,
        @NotNull
        String address,
        @Min(5)
        int capacity,
        String description
) {
}
