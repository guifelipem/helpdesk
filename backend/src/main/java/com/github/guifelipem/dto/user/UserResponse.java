package com.github.guifelipem.dto.user;

import com.github.guifelipem.enums.UserRole;

public record UserResponse(
        Long id,
        String name,
        String email,
        UserRole role
) {}
