package com.github.guifelipem.dto.ticket;

import com.github.guifelipem.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Novo status solicitado para o chamado")
public record UpdateTicketStatusRequest(
        @Schema(description = "Novo status solicitado por uma ação válida do suporte. Transições do cliente possuem endpoints específicos.", example = "WAITING_CLIENT")
        @NotNull
        TicketStatus status
) {
}
