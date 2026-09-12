package com.github.guifelipem.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para alteração da senha")
public record ChangePasswordRequest(
        @Schema(description = "Senha atual", format = "password")
        @NotBlank
        @Size(max = 72)
        String currentPassword,

        @Schema(description = "Nova senha com no mínimo 6 caracteres", format = "password", minLength = 6)
        @NotBlank
        @Size(min = 6, max = 72)
        String newPassword
) {}
