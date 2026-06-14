package com.github.guifelipem.dto.auth;

public record RegisterResponse(

        Long id,
        String name,
        String email,
        String role
) {}
