package com.github.guifelipem.service;

import com.github.guifelipem.dto.auth.LoginRequest;
import com.github.guifelipem.dto.auth.LoginResponse;
import com.github.guifelipem.dto.auth.RegisterRequest;
import com.github.guifelipem.dto.auth.RegisterResponse;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.Role;
import com.github.guifelipem.exception.EmailAlreadyExistsException;
import com.github.guifelipem.exception.InvalidCredentialsException;
import com.github.guifelipem.repository.UserRepository;
import com.github.guifelipem.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email já cadastrado");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.CLIENT)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );
    }

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Email ou senha inválidos"));

        boolean passwordMatches = passwordEncoder.matches(
                request.password(),
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            throw new InvalidCredentialsException("Email ou senha inválidos");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new LoginResponse(token);
    }
}
