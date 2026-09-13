package com.github.guifelipem.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "Token JWT para uso no header Authorization", example = "eyJhbGciOiJIUzI1NiJ9...")
        String token
) {}
