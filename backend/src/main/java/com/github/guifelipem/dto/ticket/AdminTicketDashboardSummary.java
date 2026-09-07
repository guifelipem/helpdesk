package com.github.guifelipem.dto.ticket;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Contadores globais de chamados para o dashboard administrativo")
public record AdminTicketDashboardSummary(
        long totalActive,
        long unassigned,
        long inProgress,
        long waitingClient,
        long waitingAgent,
        long resolved
) {}
