package org.example.handler.exception;

public class CounterException extends RuntimeException {
    public CounterException(String message, Throwable cause) {
        super(message, cause);
    }
}
