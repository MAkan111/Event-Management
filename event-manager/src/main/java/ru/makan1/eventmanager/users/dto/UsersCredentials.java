package ru.makan1.eventmanager.users.dto;

import jakarta.validation.constraints.NotBlank;

public record UsersCredentials(
        @NotBlank
        String login,

        @NotBlank
        String password
) {
}
