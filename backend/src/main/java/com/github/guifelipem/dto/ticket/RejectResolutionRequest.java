package com.github.guifelipem.dto.ticket;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Justificativa para rejeição da resolução do chamado")
public record RejectResolutionRequest(

        @Schema(description = "Motivo pelo qual a resolução foi rejeitada", example = "O problema ainda acontece.")
        @NotBlank(message = "A justificativa é obrigatória")
        @Size(max = 2000, message = "A justificativa deve ter no máximo 2000 caracteres")
        String reason
) {
}
