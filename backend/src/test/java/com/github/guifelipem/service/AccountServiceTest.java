package com.github.guifelipem.service;

import com.github.guifelipem.dto.account.AccountResponse;
import com.github.guifelipem.dto.account.ChangePasswordRequest;
import com.github.guifelipem.dto.account.UpdateAccountRequest;
import com.github.guifelipem.dto.account.UpdateAccountResponse;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.UserRole;
import com.github.guifelipem.exception.EmailAlreadyExistsException;
import com.github.guifelipem.exception.InvalidCurrentPasswordException;
import com.github.guifelipem.repository.UserRepository;
import com.github.guifelipem.security.AuthenticatedUserProvider;
import com.github.guifelipem.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AccountService accountService;

    @Test
    void shouldReturnAuthenticatedAccountForAnyRole() {
        User user = buildUser(UserRole.ADMIN);
        when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(user);

        AccountResponse response = accountService.getAccount();

        assertEquals(user.getId(), response.id());
        assertEquals(user.getName(), response.name());
        assertEquals(user.getEmail(), response.email());
        assertEquals(UserRole.ADMIN, response.role());
    }

    @Test
    void shouldUpdateNameAndEmailAndIssueTokenForNewEmail() {
        User user = buildUser(UserRole.AGENT);
        UpdateAccountRequest request = new UpdateAccountRequest("  Maria Souza  ", "maria.souza@example.com");
        when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(user);
        when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);
        when(jwtService.generateToken(request.email())).thenReturn("new-token");

        UpdateAccountResponse response = accountService.updateAccount(request);

        assertEquals("Maria Souza", response.name());
        assertEquals(request.email(), response.email());
        assertEquals(UserRole.AGENT, response.role());
        assertEquals("new-token", response.token());
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectEmailAlreadyUsedByAnotherAccount() {
        User user = buildUser(UserRole.CLIENT);
        UpdateAccountRequest request = new UpdateAccountRequest("Maria", "used@example.com");
        when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(user);
        when(userRepository.existsByEmailIgnoreCase(request.email())).thenReturn(true);

        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> accountService.updateAccount(request)
        );

        assertEquals("Email já cadastrado", exception.getMessage());
        verify(userRepository, never()).save(user);
    }

    @Test
    void shouldChangePasswordWhenCurrentPasswordMatches() {
        User user = buildUser(UserRole.CLIENT);
        ChangePasswordRequest request = new ChangePasswordRequest("old-password", "new-password");
        when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(user);
        when(passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.encode(request.newPassword())).thenReturn("new-hash");

        accountService.changePassword(request);

        assertEquals("new-hash", user.getPasswordHash());
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectPasswordChangeWhenCurrentPasswordDoesNotMatch() {
        User user = buildUser(UserRole.CLIENT);
        ChangePasswordRequest request = new ChangePasswordRequest("wrong-password", "new-password");
        when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(user);
        when(passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())).thenReturn(false);

        InvalidCurrentPasswordException exception = assertThrows(
                InvalidCurrentPasswordException.class,
                () -> accountService.changePassword(request)
        );

        assertEquals("Senha atual incorreta", exception.getMessage());
        verify(passwordEncoder, never()).encode(request.newPassword());
        verify(userRepository, never()).save(user);
    }

    private User buildUser(UserRole role) {
        return User.builder()
                .id(1L)
                .name("Maria Silva")
                .email("maria@example.com")
                .passwordHash("old-hash")
                .role(role)
                .active(true)
                .build();
    }
}
