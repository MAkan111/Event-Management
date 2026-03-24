package ru.makan1.eventmanagement.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.makan1.eventmanagement.users.dto.ErrorMessageResponse;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice(basePackages = "ru.makan1.eventmanagement.event.controller")
public class EventExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleEntityNotFoundException(EntityNotFoundException e) {
        log.error("Entity not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorMessageResponse(
                "Сущность не найдена",
                e.getMessage(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorMessageResponse> handleBadRequestException(RuntimeException e) {
        log.error("Bad request: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessageResponse(
                "Некорректный запрос",
                e.getMessage(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorMessageResponse> handleSecurityException(SecurityException e) {
        log.error("Access denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorMessageResponse(
                "Недостаточно прав для выполнения операции",
                e.getMessage(),
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessageResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation error: {}", e.getMessage());
        String details = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorMessageResponse(
                "Некорректный запрос",
                details,
                LocalDateTime.now()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessageResponse> handleException(Exception e) {
        log.error("Unexpected exception: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorMessageResponse(
                "Внутренняя ошибка сервера",
                e.getMessage(),
                LocalDateTime.now()
        ));
    }
}
