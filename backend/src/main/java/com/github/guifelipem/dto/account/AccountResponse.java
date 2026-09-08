package com.github.guifelipem.dto.account;

import com.github.guifelipem.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados da conta do usuário autenticado")
public record AccountResponse(
        Long id,
        String name,
        String email,
        UserRole role
) {}
