package ru.makan1.eventmanager.users.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsersRegistration(
        @NotBlank(message = "Имя пользователя не может быть пустым")
        @Size(min = 3, max = 20, message = "Имя пользователя должно содержать от 3 до 20 символов")
        String login,

        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 8, max = 100, message = "Пароль должен содержать от 8 до 100 символов")
        String password,

        @Min(18)
        int age
) {
}
