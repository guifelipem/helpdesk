package com.github.guifelipem.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Conflito que exige a redistribuição dos chamados do agente")
public record UserActiveTicketsConflictResponse(
        LocalDateTime timestamp,
        int status,
        String message,
        long activeTicketCount
) {}
