package com.github.guifelipem.dto.ticket;

import com.github.guifelipem.enums.TicketPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Dados para abertura de um chamado")
public record CreateTicketRequest(

        @Schema(description = "Resumo do problema", example = "Impressora não responde")
        @NotBlank
        String title,

        @Schema(description = "Detalhamento do problema", example = "A impressora do financeiro aparece offline desde esta manhã.")
        @NotBlank
        String description,

        @Schema(description = "Prioridade informada pelo cliente", example = "MEDIUM")
        @NotNull
        TicketPriority priority
) {
}
