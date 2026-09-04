package com.github.guifelipem.service;

import com.github.guifelipem.dto.user.UpdateUserRoleRequest;
import com.github.guifelipem.dto.user.UserResponse;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.UserRole;
import com.github.guifelipem.exception.ForbiddenException;
import com.github.guifelipem.exception.UserNotFoundException;
import com.github.guifelipem.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldListUsersWithFiltersAndPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = buildUser(1L, UserRole.AGENT);

        when(userRepository.findAllWithFilters(UserRole.AGENT, "maria", pageable))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));

        Page<UserResponse> response = userService.findAll(UserRole.AGENT, "maria", pageable);

        assertEquals(1, response.getTotalElements());
        assertEquals(user.getId(), response.getContent().getFirst().id());
        assertEquals(user.getName(), response.getContent().getFirst().name());
        assertEquals(user.getEmail(), response.getContent().getFirst().email());
        assertEquals(user.getRole(), response.getContent().getFirst().role());
    }

    @Test
    void shouldUpdateUserRole() {
        User user = buildUser(1L, UserRole.CLIENT);
        UpdateUserRoleRequest request = new UpdateUserRoleRequest(UserRole.AGENT);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.updateRole(user.getId(), request);

        assertEquals(UserRole.AGENT, response.role());
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectRoleChangeWhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.updateRole(1L, new UpdateUserRoleRequest(UserRole.AGENT))
        );

        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    @Test
    void shouldRejectChangingAdministratorRole() {
        User administrator = buildUser(1L, UserRole.ADMIN);
        when(userRepository.findById(administrator.getId())).thenReturn(Optional.of(administrator));

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> userService.updateRole(administrator.getId(), new UpdateUserRoleRequest(UserRole.AGENT))
        );

        assertEquals("Não é permitido alterar a role de um administrador", exception.getMessage());
        verify(userRepository, never()).save(administrator);
    }

    @Test
    void shouldRejectAssigningAdministratorRole() {
        User user = buildUser(1L, UserRole.CLIENT);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> userService.updateRole(user.getId(), new UpdateUserRoleRequest(UserRole.ADMIN))
        );

        assertEquals("Não é permitido atribuir a role de administrador", exception.getMessage());
        verify(userRepository, never()).save(user);
    }

    private User buildUser(Long id, UserRole role) {
        return User.builder()
                .id(id)
                .name("Maria Silva")
                .email("maria@example.com")
                .role(role)
                .build();
    }
}
