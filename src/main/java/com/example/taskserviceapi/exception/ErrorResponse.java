package com.example.taskserviceapi.exception;

import java.time.Instant;

public record ErrorResponse(
        String message,
        int status,
        Instant timestamp
) {
    public static ErrorResponse from(String message, int status) {
        return new ErrorResponse(message, status, Instant.now());
    }
}
