package com.github.guifelipem.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TicketStatusTest {

    @Test
    void shouldAllowOnlySupportWorkflowTransitions() {
        assertTrue(TicketStatus.OPEN.canSupportTransitionTo(TicketStatus.IN_PROGRESS));
        assertTrue(TicketStatus.IN_PROGRESS.canSupportTransitionTo(TicketStatus.WAITING_CLIENT));
        assertTrue(TicketStatus.IN_PROGRESS.canSupportTransitionTo(TicketStatus.RESOLVED));
        assertTrue(TicketStatus.WAITING_CLIENT.canSupportTransitionTo(TicketStatus.IN_PROGRESS));
        assertTrue(TicketStatus.WAITING_AGENT.canSupportTransitionTo(TicketStatus.IN_PROGRESS));

        assertFalse(TicketStatus.WAITING_CLIENT.canSupportTransitionTo(TicketStatus.WAITING_AGENT));
        assertFalse(TicketStatus.WAITING_CLIENT.canSupportTransitionTo(TicketStatus.RESOLVED));
        assertFalse(TicketStatus.WAITING_AGENT.canSupportTransitionTo(TicketStatus.RESOLVED));
        assertFalse(TicketStatus.RESOLVED.canSupportTransitionTo(TicketStatus.IN_PROGRESS));
    }

    @Test
    void shouldAllowOnlyClientWorkflowTransitions() {
        assertTrue(TicketStatus.WAITING_CLIENT.canClientTransitionTo(TicketStatus.WAITING_AGENT));
        assertTrue(TicketStatus.RESOLVED.canClientTransitionTo(TicketStatus.CLOSED));
        assertTrue(TicketStatus.RESOLVED.canClientTransitionTo(TicketStatus.IN_PROGRESS));

        assertFalse(TicketStatus.IN_PROGRESS.canClientTransitionTo(TicketStatus.WAITING_AGENT));
        assertFalse(TicketStatus.WAITING_AGENT.canClientTransitionTo(TicketStatus.IN_PROGRESS));
        assertFalse(TicketStatus.CLOSED.canClientTransitionTo(TicketStatus.IN_PROGRESS));
    }
}
