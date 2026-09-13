package com.github.guifelipem.dto.ticket;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Quantidade de chamados ativos atribuídos a um agente")
public record AgentActiveTicketsResponse(
        Long agentId,
        String agentName,
        long activeTickets
) {}
