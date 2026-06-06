package com.skybooker.flight_service.exception;


public class InsufficientSeatsException extends RuntimeException {
    public InsufficientSeatsException(String message) {
        super(message);
    }

    public InsufficientSeatsException(String message, Throwable cause) {
        super(message, cause);
    }
}
