package com.github.guifelipem.dto.ticket;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Painel pessoal de trabalho do agente autenticado")
public record AgentTicketDashboardResponse(
        long active,
        long waitingClient,
        long waitingAgent,
        long resolvedAwaitingConfirmation,
        long available,
        List<TicketResponse> priorityNow
) {}
