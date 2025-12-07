package ru.itmo.music.music_service.api.error;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.itmo.music.music_service.application.exception.ConflictException;
import ru.itmo.music.music_service.application.exception.DomainValidationException;
import ru.itmo.music.music_service.application.exception.NotFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps domain and validation exceptions to consistent HTTP error responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "not_found", ex.getMessage(), null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        return build(HttpStatus.CONFLICT, "conflict", ex.getMessage(), null);
    }

    @ExceptionHandler(DomainValidationException.class)
    public ResponseEntity<ErrorResponse> handleDomainValidation(DomainValidationException ex) {
        return build(HttpStatus.BAD_REQUEST, "validation_failed", ex.getMessage(), ex.getDetails());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, Object> details = new HashMap<>();
        if (ex.getBindingResult().getFieldError() != null) {
            details.put("field", ex.getBindingResult().getFieldError().getField());
            details.put("rejected", ex.getBindingResult().getFieldError().getRejectedValue());
            details.put("message", ex.getBindingResult().getFieldError().getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "validation_failed", "Validation error", details);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, Object> details = new HashMap<>();
        details.put("violations", ex.getConstraintViolations());
        return build(HttpStatus.BAD_REQUEST, "validation_failed", "Validation error", details);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleOther(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "internal_error", "Unexpected error", null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message, Map<String, Object> details) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message, details));
    }
}
