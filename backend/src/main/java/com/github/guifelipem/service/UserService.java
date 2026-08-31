package com.github.guifelipem.service;

import com.github.guifelipem.dto.user.UpdateUserRoleRequest;
import com.github.guifelipem.dto.user.UserResponse;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.UserRole;
import com.github.guifelipem.exception.ForbiddenException;
import com.github.guifelipem.exception.UserNotFoundException;
import com.github.guifelipem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

        private final UserRepository userRepository;

        public Page<UserResponse> findAll(UserRole role, String search, Pageable pageable) {
                Page<User> users = userRepository.findAllWithFilters(role, search, pageable);

                return users.map(this::toResponse);
        }

        public UserResponse updateRole(Long userId, UpdateUserRoleRequest request) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

                if (user.getRole() == UserRole.ADMIN) {
                        throw new ForbiddenException("Não é permitido alterar a role de um administrador");
                }

                if (request.role() == UserRole.ADMIN) {
                        throw new ForbiddenException("Não é permitido atribuir a role de administrador");
                }

                user.setRole(request.role());

                User updatedUser = userRepository.save(user);

                return toResponse(updatedUser);
        }

        private UserResponse toResponse(User user) {
                return new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole()
                );
        }
}
