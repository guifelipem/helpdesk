package com.github.guifelipem.dto.ticket;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Indicadores pessoais do agente no período selecionado")
public record AgentTicketPerformanceResponse(
        LocalDateTime from,
        LocalDateTime to,
        long resolved,
        Long averageResolutionMinutes
) {}
