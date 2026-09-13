package com.github.guifelipem.dto.ticket;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Agente de destino da transferência")
public record TransferTicketRequest(
        @Schema(description = "ID de um AGENT ativo", example = "7")
        @NotNull
        Long agentId
) {}
