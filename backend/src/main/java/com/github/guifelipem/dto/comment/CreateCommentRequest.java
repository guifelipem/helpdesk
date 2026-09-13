package com.github.guifelipem.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados do novo comentário")
public record CreateCommentRequest(

        @Schema(description = "Texto do comentário", example = "O problema continua após reiniciar o equipamento.")
        @NotBlank(message = "A mensagem é obrigatória")
        @Size(max = 5000, message = "A mensagem deve ter no máximo 5000 caracteres")
        String message,

        @Schema(description = "Se verdadeiro, o comentário não é exibido aos clientes. CLIENT não pode usar true.", example = "false")
        @NotNull(message = "Informe se o comentário é interno")
        Boolean isInternal
) {}
