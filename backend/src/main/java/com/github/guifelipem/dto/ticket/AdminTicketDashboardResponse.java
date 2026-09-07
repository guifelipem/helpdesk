package com.github.guifelipem.dto.ticket;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Indicadores do dashboard administrativo de chamados")
public record AdminTicketDashboardResponse(
        long totalActive,
        long unassigned,
        long inProgress,
        long waitingClient,
        long waitingAgent,
        long resolved,
        List<AgentActiveTicketsResponse> activeByAgent,
        List<TicketResponse> attentionTickets
) {}
