package ru.makan1.eventmanager.users.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SecurityErrorResponse {
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
}
