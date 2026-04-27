package ru.makan1.eventmanager.users.dto;

import ru.makan1.eventmanager.users.enums.UserRole;

public record UsersResponse(
        Long id,
        String login,
        int age,
        UserRole role
) {
}
