package com.github.guifelipem.dto.ticket;

import com.github.guifelipem.enums.TicketPriority;
import com.github.guifelipem.enums.TicketStatus;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados completos de um chamado")
public record TicketResponse(

        Long id,
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        UserSummaryResponse createdBy,
        @Schema(description = "Responsável atual; nulo enquanto o chamado não foi assumido", nullable = true) UserSummaryResponse assignedTo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
