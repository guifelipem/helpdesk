package com.github.guifelipem.controller;

import com.github.guifelipem.dto.auth.RegisterRequest;
import com.github.guifelipem.dto.auth.RegisterResponse;
import com.github.guifelipem.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public RegisterResponse register(
            @RequestBody @Valid RegisterRequest request) {
        return authService.register(request);
    }
}
