package com.kbhc.codetest.exception;

public class DuplicateException extends RuntimeException {
    public DuplicateException(String message) {
        super("Duplicated : " + message);
    }
}
