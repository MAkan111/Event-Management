package ru.makan1.eventmanagement.event.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EventRequest(
        @NotBlank(message = "Название мероприятия обязательно")
        String name,

        @NotNull(message = "Максимальное количество мест обязательно")
        @Min(value = 1, message = "Максимальное количество мест должно быть > 0")
        Integer maxPlaces,

        @NotNull(message = "Дата мероприятия обязательна")
        @Future(message = "Дата мероприятия не может быть в прошлом")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        LocalDateTime date,

        @NotNull(message = "Стоимость обязательна")
        @DecimalMin(value = "0", message = "Стоимость должна быть >= 0")
        BigDecimal cost,

        @Min(value = 30, message = "Длительность должна быть >= 30 минут")
        int duration,

        @NotNull(message = "ID локации обязателен")
        Long locationId
) {
}
