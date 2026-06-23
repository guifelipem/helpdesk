package com.github.guifelipem.dto.auth;

import com.github.guifelipem.enums.UserRole;

public record MeResponse(
        Long id,
        String name,
        String email,
        UserRole role
) {}
