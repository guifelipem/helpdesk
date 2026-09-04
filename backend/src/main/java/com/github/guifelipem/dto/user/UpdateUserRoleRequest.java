package com.github.guifelipem.dto.user;

import com.github.guifelipem.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Novo perfil do usuário")
public record UpdateUserRoleRequest(
        @Schema(description = "Somente CLIENT ou AGENT são aceitos pela regra atual", example = "AGENT")
        @NotNull
        UserRole role
) {}
