package org.example.exception;

public class BookParseException extends RuntimeException {
    public BookParseException(String message) {
        super(message);
    }

    public BookParseException(String message, Exception exception) {
        super(message, exception);
    }
}