package com.github.guifelipem.dto.ticket;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Contadores das filas operacionais do agente autenticado")
public record TicketQueueSummaryResponse(
        long available,
        long myTickets,
        long waitingClient,
        long waitingAgent,
        long resolved
) {}
