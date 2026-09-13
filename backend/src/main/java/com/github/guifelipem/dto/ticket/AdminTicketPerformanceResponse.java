package com.github.guifelipem.dto.ticket;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Indicadores históricos de chamados em um intervalo")
public record AdminTicketPerformanceResponse(
        LocalDateTime from,
        LocalDateTime to,
        long created,
        long resolved,
        long closed,
        Long averageResolutionMinutes
) {}
