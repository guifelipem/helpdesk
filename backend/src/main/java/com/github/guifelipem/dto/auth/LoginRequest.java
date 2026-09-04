package com.github.guifelipem.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais para autenticação")
public record LoginRequest(

        @Schema(description = "E-mail cadastrado", example = "cliente@example.com")
        @Email
        @NotBlank
        String email,

        @Schema(description = "Senha da conta", example = "senha123", format = "password")
        @NotBlank
        String password
) {}
