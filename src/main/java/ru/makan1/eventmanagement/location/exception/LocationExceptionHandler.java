package ru.makan1.eventmanagement.location.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.makan1.eventmanagement.location.dto.ErrorMessageResponse;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class LocationExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessageResponse> handleException(Exception e) {
        log.error("Exception occured: {} {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
        var errorMessageResponse = new ErrorMessageResponse(
                "Внутренняя ошибка сервера",
                e.getMessage(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(errorMessageResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleEntityNotFoundException(EntityNotFoundException e) {
        log.error("Entity not found exception: {} {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
        var errorMessageResponse = new ErrorMessageResponse(
                "Ошибка поиска локации",
                e.getMessage(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(errorMessageResponse, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("Validation exception: {} {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
        Map<String, String> errorMessageResponse = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach((fieldError) -> {
            errorMessageResponse.put(fieldError.getField(), fieldError.getDefaultMessage());
        });

        return errorMessageResponse;
    }
}
