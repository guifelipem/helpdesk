package com.github.guifelipem.service;

import com.github.guifelipem.dto.auth.*;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.UserRole;
import com.github.guifelipem.exception.EmailAlreadyExistsException;
import com.github.guifelipem.exception.InvalidCredentialsException;
import com.github.guifelipem.repository.UserRepository;
import com.github.guifelipem.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

        @Mock
        private UserRepository userRepository;

        @Mock
        private PasswordEncoder passwordEncoder;

        @Mock
        private JwtService jwtService;

        @InjectMocks
        private AuthService authService;

        @Test
        void shouldRegisterUserSuccessfully() {
                RegisterRequest request =
                        new RegisterRequest("Fulano", "fulano@email.com", "123456");

                when(userRepository.existsByEmail(request.email()))
                        .thenReturn(false);

                when(passwordEncoder.encode(request.password()))
                        .thenReturn("SenhaCodificada");

                when(userRepository.save(any(User.class)))
                        .thenAnswer(invocation -> {
                                User user = invocation.getArgument(0);
                                user.setId(1L);
                                return user;
                        });

                RegisterResponse response = authService.register(request);

                ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

                verify(userRepository).save(userCaptor.capture());

                User userToSave = userCaptor.getValue();

                assertEquals(1L, response.id());
                assertEquals("fulano@email.com", response.email());
                assertEquals(request.name(), response.name());

                assertEquals(request.name(), userToSave.getName());
                assertEquals(request.email(), userToSave.getEmail());
                assertEquals("SenhaCodificada", userToSave.getPasswordHash());
                assertEquals(UserRole.CLIENT, userToSave.getRole());
                assertNotNull(userToSave.getCreatedAt());
        }

        @Test
        void shouldThrowEmailAlreadyExistsExceptionWhenEmailAlreadyExists() {
                RegisterRequest request =
                        new RegisterRequest("Fulano", "fulano@email.com", "123456");

                when(userRepository.existsByEmail(request.email()))
                        .thenReturn(true);

                EmailAlreadyExistsException exception = assertThrows(
                        EmailAlreadyExistsException.class,
                        () -> authService.register(request)
                );

                assertEquals(
                        "Email já cadastrado",
                        exception.getMessage()
                );
        }

        @Test
        void shouldLoginSuccessfully() {
                LoginRequest request =
                        new LoginRequest("fulano@email.com", "123456");

                User user = User.builder()
                        .id(1L)
                        .email("fulano@email.com")
                        .passwordHash("SenhaCodificada")
                        .build();

                when(userRepository.findByEmail(request.email()))
                        .thenReturn(Optional.of(user));

                when(passwordEncoder.matches(request.password(), user.getPasswordHash()))
                        .thenReturn(true);

                when(jwtService.generateToken(user.getEmail()))
                        .thenReturn("token-teste");

                LoginResponse response = authService.login(request);

                verify(jwtService).generateToken(user.getEmail());

                assertEquals("token-teste", response.token());
        }

        @Test
        void shouldThrowInvalidCredentialsExceptionWhenEmailDoesNotExist() {
                LoginRequest request =
                        new LoginRequest("fulano@email.com", "123456");

                when(userRepository.findByEmail(request.email()))
                        .thenReturn(Optional.empty());

                InvalidCredentialsException exception = assertThrows(
                        InvalidCredentialsException.class,
                        () -> authService.login(request)
                );

                assertEquals(
                        "Email ou senha inválidos",
                        exception.getMessage()
                );
        }

        @Test
        void shouldThrowInvalidCredentialsExceptionWhenPasswordIsIncorrect() {
                LoginRequest request =
                        new LoginRequest("fulano@email.com", "123456");

                User user = User.builder()
                        .id(1L)
                        .email("fulano@email.com")
                        .passwordHash("SenhaCodificada")
                        .build();

                when(userRepository.findByEmail(request.email()))
                        .thenReturn(Optional.of(user));

                when(passwordEncoder.matches(request.password(), user.getPasswordHash()))
                        .thenReturn(false);

                InvalidCredentialsException exception = assertThrows(
                        InvalidCredentialsException.class,
                        () -> authService.login(request)
                );

                assertEquals(
                        "Email ou senha inválidos",
                        exception.getMessage()
                );
        }

        @Test
        void shouldReturnAuthenticatedUserSuccessfully() {
                User user = User.builder()
                        .id(1L)
                        .name("Fulano")
                        .email("fulano@email.com")
                        .role(UserRole.CLIENT)
                        .build();

                when(userRepository.findByEmail(user.getEmail()))
                        .thenReturn(Optional.of(user));

                MeResponse response = authService.me(user.getEmail());

                assertEquals(user.getId(), response.id());
                assertEquals(user.getName(), response.name());
                assertEquals(user.getEmail(), response.email());
                assertEquals(user.getRole(), response.role());
        }

        @Test
        void shouldThrowUsernameNotFoundExceptionWhenUserDoesNotExist() {
                String email = "fulano@email.com";

                when(userRepository.findByEmail(email))
                        .thenReturn(Optional.empty());

                UsernameNotFoundException exception = assertThrows(
                        UsernameNotFoundException.class,
                        () -> authService.me(email)
                );

                assertEquals(
                        "Usuário não encontrado",
                        exception.getMessage()
                );
        }
}
