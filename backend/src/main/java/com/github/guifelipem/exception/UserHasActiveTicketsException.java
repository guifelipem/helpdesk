package com.github.guifelipem.exception;

public class UserHasActiveTicketsException extends RuntimeException {

    private final long activeTicketCount;

    public UserHasActiveTicketsException(String message) {
        this(message, 0);
    }

    public UserHasActiveTicketsException(String message, long activeTicketCount) {
        super(message);
        this.activeTicketCount = activeTicketCount;
    }

    public long getActiveTicketCount() {
        return activeTicketCount;
    }
}
