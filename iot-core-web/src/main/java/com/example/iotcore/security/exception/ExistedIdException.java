package com.example.iotcore.security.exception;

public class ExistedIdException extends RuntimeException {
    public ExistedIdException(String message) {
        super(message);
    }
}
