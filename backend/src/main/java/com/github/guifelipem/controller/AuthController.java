package com.github.guifelipem.controller;

import com.github.guifelipem.dto.auth.*;
import com.github.guifelipem.service.AuthService;
import com.github.guifelipem.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Cadastro, autenticação e consulta da identidade atual")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @SecurityRequirements
    @Operation(summary = "Cadastrar cliente", description = "Cria uma conta com o perfil CLIENT. Não requer autenticação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente cadastrado",
                    content = @Content(schema = @Schema(implementation = RegisterResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de cadastro inválidos"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public RegisterResponse register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Nome, e-mail único e senha do novo cliente", required = true)
            @RequestBody @Valid RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Autenticar usuário", description = "Valida e-mail e senha e retorna um token JWT. Não requer autenticação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticação realizada",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados de login inválidos"),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LoginResponse> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Credenciais da conta", required = true)
            @RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Consultar usuário autenticado", description = "Retorna os dados da conta identificada pelo token JWT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário autenticado",
                    content = @Content(schema = @Schema(implementation = MeResponse.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MeResponse> me(Authentication authentication) {
        String email = authentication.getName();

        return ResponseEntity.ok(authService.me(email));
    }
}
