package com.github.guifelipem.exception;

public class InvalidCurrentPasswordException extends RuntimeException {

    public InvalidCurrentPasswordException(String message) {
        super(message);
    }
}
