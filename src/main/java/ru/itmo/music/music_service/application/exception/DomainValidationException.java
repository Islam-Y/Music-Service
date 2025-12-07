package ru.itmo.music.music_service.application.exception;

import java.util.Map;

public class DomainValidationException extends RuntimeException {

    private final Map<String, Object> details;

    public DomainValidationException(String message) {
        super(message);
        this.details = null;
    }

    public DomainValidationException(String message, Map<String, Object> details) {
        super(message);
        this.details = details;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
