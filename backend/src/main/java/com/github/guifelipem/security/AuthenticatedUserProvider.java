package com.github.guifelipem.security;

import com.github.guifelipem.entity.User;
import com.github.guifelipem.exception.UserNotFoundException;
import com.github.guifelipem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthenticatedUserProvider {

    private final UserRepository userRepository;

    public User getAuthenticatedUser() {
        String email = authenticatedEmail();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
    }

    public User getAuthenticatedUserForUpdate() {
        String email = authenticatedEmail();

        return userRepository.findByEmailForUpdate(email)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));
    }

    private String authenticatedEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
