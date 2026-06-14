package com.github.guifelipem.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCommentRequest(

        @NotBlank(message = "A mensagem é obrigatória")
        String message,

        @NotNull(message = "Informe se o comentário é interno")
        Boolean isInternal
) {}
