package com.github.guifelipem.dto.user;

import com.github.guifelipem.enums.UserRole;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
        @NotNull
        UserRole role
) {}
