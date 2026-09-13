package com.github.guifelipem.dto.account;

import com.github.guifelipem.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Conta atualizada e novo token, necessário quando o e-mail é alterado")
public record UpdateAccountResponse(
        Long id,
        String name,
        String email,
        UserRole role,
        String token
) {}
