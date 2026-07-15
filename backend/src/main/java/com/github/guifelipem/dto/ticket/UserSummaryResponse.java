package com.github.guifelipem.dto.ticket;

import com.github.guifelipem.enums.UserRole;

public record UserSummaryResponse(
        Long id,
        String name,
        UserRole role
) {
}
