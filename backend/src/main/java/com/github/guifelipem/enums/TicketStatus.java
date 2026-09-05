package com.github.guifelipem.enums;

public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    WAITING_CLIENT,
    WAITING_AGENT,
    RESOLVED,
    CLOSED;

    public boolean canSupportTransitionTo(TicketStatus target) {

        return switch (this) {

            case OPEN -> target == IN_PROGRESS;

            case IN_PROGRESS -> target == WAITING_CLIENT || target == RESOLVED;

            case WAITING_CLIENT, WAITING_AGENT -> target == IN_PROGRESS;

            case RESOLVED, CLOSED -> false;
        };
    }

    public boolean canClientTransitionTo(TicketStatus target) {

        return switch (this) {

            case WAITING_CLIENT -> target == WAITING_AGENT;

            case RESOLVED -> target == CLOSED || target == IN_PROGRESS;

            case OPEN, IN_PROGRESS, WAITING_AGENT, CLOSED -> false;
        };
    }
}
