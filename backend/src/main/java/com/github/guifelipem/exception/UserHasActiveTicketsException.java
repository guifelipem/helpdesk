package com.github.guifelipem.exception;

public class UserHasActiveTicketsException extends RuntimeException {

    public UserHasActiveTicketsException(String message) {
        super(message);
    }
}
