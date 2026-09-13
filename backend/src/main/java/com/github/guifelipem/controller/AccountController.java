package com.github.guifelipem.controller;

import com.github.guifelipem.dto.account.AccountResponse;
import com.github.guifelipem.dto.account.ChangePasswordRequest;
import com.github.guifelipem.dto.account.UpdateAccountRequest;
import com.github.guifelipem.dto.account.UpdateAccountResponse;
import com.github.guifelipem.exception.ErrorResponse;
import com.github.guifelipem.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@Tag(name = "Minha conta", description = "Consulta e atualização da conta autenticada para CLIENT, AGENT e ADMIN")
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    @Operation(summary = "Consultar minha conta", description = "Retorna nome, e-mail e role (somente leitura) do usuário autenticado.")
    public AccountResponse getAccount() {
        return accountService.getAccount();
    }

    @PatchMapping
    @Operation(summary = "Atualizar minha conta", description = "Altera nome e e-mail. A role não pode ser alterada. Retorna um novo JWT para manter a sessão válida após troca do e-mail.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta atualizada", content = @Content(schema = @Schema(implementation = UpdateAccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Nome ou e-mail inválido"),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public UpdateAccountResponse updateAccount(@Valid @RequestBody UpdateAccountRequest request) {
        return accountService.updateAccount(request);
    }

    @PatchMapping("/password")
    @Operation(summary = "Alterar minha senha", description = "Altera a senha somente após validar a senha atual.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha alterada"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou senha atual incorreta", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        accountService.changePassword(request);
        return ResponseEntity.noContent().build();
    }
}
