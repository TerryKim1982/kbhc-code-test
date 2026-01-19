package com.kbhc.codetest.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super("Invalid Token: " + message);
    }
}
