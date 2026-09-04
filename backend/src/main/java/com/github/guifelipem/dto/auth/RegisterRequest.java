package com.github.guifelipem.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro de uma conta CLIENT")
public record RegisterRequest(

        @Schema(description = "Nome do cliente", example = "Maria Silva")
        @NotBlank
        String name,

        @Schema(description = "E-mail único da conta", example = "maria@example.com")
        @NotBlank
        @Email
        String email,

        @Schema(description = "Senha com no mínimo 6 caracteres", example = "senha123", format = "password", minLength = 6)
        @NotBlank
        @Size(min = 6)
        String password
) {}
