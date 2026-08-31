package com.github.guifelipem.controller;

import com.github.guifelipem.dto.user.UpdateUserRoleRequest;
import com.github.guifelipem.dto.user.UserResponse;
import com.github.guifelipem.enums.UserRole;
import com.github.guifelipem.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

        private final UserService userService;

        @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
        public Page<UserResponse> findAll(
                @RequestParam(required = false) UserRole role,
                @RequestParam(required = false) String search,
                Pageable pageable
        ) {
                return userService.findAll(role, search, pageable);
        }

        @PatchMapping("/{id}/role")
        @PreAuthorize("hasRole('ADMIN')")
        public UserResponse updateRole(
                @PathVariable Long id,
                @Valid @RequestBody UpdateUserRoleRequest request
        ) {
                return userService.updateRole(id, request);
        }
}