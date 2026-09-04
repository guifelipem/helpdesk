package com.github.guifelipem.controller;

import com.github.guifelipem.dto.history.TicketHistoryResponse;
import com.github.guifelipem.exception.ErrorResponse;
import com.github.guifelipem.service.TicketHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/history")
@RequiredArgsConstructor
@Tag(name = "Histórico de chamados", description = "Eventos registrados durante o ciclo de vida dos chamados")
public class TicketHistoryController {

    private final TicketHistoryService ticketHistoryService;

    @PreAuthorize("hasAnyRole('CLIENT', 'AGENT', 'ADMIN')")
    @GetMapping
    @Operation(summary = "Consultar histórico do chamado", description = "Retorna os eventos em ordem cronológica. CLIENT acessa apenas chamado próprio. AGENT e ADMIN podem acessar chamado sem responsável ou atribuído a si, mas não um chamado atribuído a outra pessoa.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Histórico do chamado"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário sem acesso ao histórico", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Chamado não encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<TicketHistoryResponse>> findByTicket(@Parameter(description = "ID do chamado", example = "42") @PathVariable Long ticketId) {

        return ResponseEntity.ok(ticketHistoryService.findByTicket(ticketId));
    }
}
