package com.github.guifelipem.enums;

public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    WAITING_CLIENT,
    RESOLVED,
    CLOSED;

    public boolean canTransitionTo(TicketStatus target) {

        return switch (this) {

            case OPEN -> target == IN_PROGRESS;

            case IN_PROGRESS -> target == WAITING_CLIENT || target == RESOLVED;

            case WAITING_CLIENT -> target == IN_PROGRESS || target == RESOLVED;

            case RESOLVED -> target == CLOSED;

            case CLOSED -> false;
        };
    }
}
