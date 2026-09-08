package com.github.guifelipem.dto.ticket;

public record AgentTicketDashboardSummary(
        long active,
        long waitingClient,
        long waitingAgent,
        long resolved,
        long available
) {}
