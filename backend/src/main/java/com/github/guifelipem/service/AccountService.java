package com.github.guifelipem.service;

import com.github.guifelipem.dto.account.AccountResponse;
import com.github.guifelipem.dto.account.ChangePasswordRequest;
import com.github.guifelipem.dto.account.UpdateAccountRequest;
import com.github.guifelipem.dto.account.UpdateAccountResponse;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.exception.EmailAlreadyExistsException;
import com.github.guifelipem.exception.InvalidCurrentPasswordException;
import com.github.guifelipem.repository.UserRepository;
import com.github.guifelipem.security.AuthenticatedUserProvider;
import com.github.guifelipem.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public AccountResponse getAccount() {
        return toResponse(authenticatedUserProvider.getAuthenticatedUser());
    }

    @Transactional
    public UpdateAccountResponse updateAccount(UpdateAccountRequest request) {
        User user = authenticatedUserProvider.getAuthenticatedUser();
        String email = request.email().trim();

        if (!user.getEmail().equalsIgnoreCase(email)
                && userRepository.existsByEmailIgnoreCase(email)) {
            throw new EmailAlreadyExistsException("Email já cadastrado");
        }

        user.setName(request.name().trim());
        user.setEmail(email);
        User updatedUser = userRepository.save(user);

        return new UpdateAccountResponse(
                updatedUser.getId(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getRole(),
                jwtService.generateToken(updatedUser.getEmail())
        );
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = authenticatedUserProvider.getAuthenticatedUser();

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCurrentPasswordException("Senha atual incorreta");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private AccountResponse toResponse(User user) {
        return new AccountResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
