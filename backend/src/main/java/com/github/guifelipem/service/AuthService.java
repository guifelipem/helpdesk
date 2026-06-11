package com.github.guifelipem.service;

import com.github.guifelipem.dto.auth.RegisterRequest;
import com.github.guifelipem.dto.auth.RegisterResponse;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.Role;
import com.github.guifelipem.exception.EmailAlreadyExistsException;
import com.github.guifelipem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
}
