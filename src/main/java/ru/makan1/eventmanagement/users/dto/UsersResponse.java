package ru.makan1.eventmanagement.users.dto;

import ru.makan1.eventmanagement.users.enums.UserRole;

public record UsersResponse(
        Long id,
        String login,
        int age,
        UserRole role
) {
}
