package com.github.guifelipem.controller;

import com.github.guifelipem.dto.user.UpdateUserRoleRequest;
import com.github.guifelipem.dto.user.BlockUserRequest;
import com.github.guifelipem.dto.user.UserActiveTicketsConflictResponse;
import com.github.guifelipem.dto.user.UserResponse;
import com.github.guifelipem.enums.UserRole;
import com.github.guifelipem.exception.ErrorResponse;
import com.github.guifelipem.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Consulta e administração de usuários")
public class UserController {

        private final UserService userService;

        @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Listar usuários", description = "Lista usuários com filtros, paginação e ordenação do Spring Data. Permitido somente para ADMIN.")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Página de usuários"),
                @ApiResponse(responseCode = "400", description = "Filtro, paginação ou ordenação inválidos"),
                @ApiResponse(responseCode = "401", description = "Autenticação necessária", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = "403", description = "Perfil diferente de ADMIN", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public Page<UserResponse> findAll(
                @Parameter(description = "Filtra pelo perfil") @RequestParam(required = false) UserRole role,
                @Parameter(description = "Busca parcial, sem diferenciar maiúsculas, no nome ou e-mail", example = "maria") @RequestParam(required = false) String search,
                @Parameter(description = "Paginação e ordenação. Use page (base zero), size e sort no formato campo,direção.")
                Pageable pageable
        ) {
                return userService.findAll(role, search, pageable);
        }

        @PatchMapping("/{id}/role")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Alterar perfil de usuário", description = "Altera um usuário entre CLIENT e AGENT. Não permite alterar um ADMIN nem atribuir o perfil ADMIN. Permitido somente para ADMIN.")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Perfil alterado", content = @Content(schema = @Schema(implementation = UserResponse.class))),
                @ApiResponse(responseCode = "400", description = "Perfil não informado ou inválido"),
                @ApiResponse(responseCode = "401", description = "Autenticação necessária", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = "403", description = "Perfil diferente de ADMIN ou alteração de/para ADMIN", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
        })
        public UserResponse updateRole(
                @Parameter(description = "ID do usuário", example = "7") @PathVariable Long id,
                @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Novo perfil CLIENT ou AGENT", required = true)
                @Valid @RequestBody UpdateUserRoleRequest request
        ) {
                return userService.updateRole(id, request);
        }

        @PatchMapping("/{id}/block")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Bloquear usuário", description = "Bloqueia um CLIENT ou AGENT. Se o AGENT possuir chamados não fechados, a chamada sem estratégia retorna 409 e activeTicketCount; repita informando TRANSFER ou RETURN_TO_QUEUE.")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Usuário bloqueado", content = @Content(schema = @Schema(implementation = UserResponse.class))),
                @ApiResponse(responseCode = "409", description = "O agente possui chamados que precisam ser redistribuídos", content = @Content(schema = @Schema(implementation = UserActiveTicketsConflictResponse.class)))
        })
        public UserResponse block(
                @Parameter(description = "ID do usuário") @PathVariable Long id,
                @RequestBody(required = false) BlockUserRequest request
        ) {
                return userService.block(id, request);
        }

        @PatchMapping("/{id}/unblock")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Desbloquear usuário", description = "Restaura o acesso de um CLIENT ou AGENT.")
        public UserResponse unblock(@Parameter(description = "ID do usuário") @PathVariable Long id) {
                return userService.unblock(id);
        }
}
