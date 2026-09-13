package com.github.guifelipem.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados editáveis da conta")
public record UpdateAccountRequest(
        @Schema(description = "Nome do usuário", example = "Maria Silva")
        @NotBlank
        @Size(max = 255)
        String name,

        @Schema(description = "E-mail único da conta", example = "maria@example.com")
        @NotBlank
        @Email
        @Size(max = 255)
        String email
) {}
