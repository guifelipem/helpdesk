package com.github.guifelipem.exception;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Erro tratado pela API")
public record ErrorResponse(
        @Schema(description = "Data e hora do erro", example = "2026-09-03T14:30:00") LocalDateTime timestamp,
        @Schema(description = "Código HTTP", example = "403") int status,
        @Schema(description = "Descrição do erro", example = "Você não tem permissão para acessar este recurso") String message
) {}
