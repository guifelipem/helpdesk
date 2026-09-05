package com.github.guifelipem.controller;

import com.github.guifelipem.dto.common.PageResponse;
import com.github.guifelipem.dto.ticket.CreateTicketRequest;
import com.github.guifelipem.dto.ticket.RejectResolutionRequest;
import com.github.guifelipem.dto.ticket.TicketResponse;
import com.github.guifelipem.dto.ticket.UpdateTicketStatusRequest;
import com.github.guifelipem.enums.TicketPriority;
import com.github.guifelipem.enums.TicketStatus;
import com.github.guifelipem.exception.ErrorResponse;
import com.github.guifelipem.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Chamados", description = "Abertura, consulta, atribuição e evolução de chamados")
public class TicketController {

    private final TicketService ticketService;

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping
    @Operation(summary = "Abrir chamado", description = "Cria um chamado OPEN para o cliente autenticado. Permitido somente para CLIENT.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Chamado criado",
                    content = @Content(schema = @Schema(implementation = TicketResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados do chamado inválidos"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Perfil diferente de CLIENT", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TicketResponse> createTicket(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Título, descrição e prioridade do chamado", required = true)
            @RequestBody @Valid CreateTicketRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.create(request));
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/me")
    @Operation(summary = "Listar meus chamados", description = "Lista, sem paginação, todos os chamados criados pelo cliente autenticado. Permitido somente para CLIENT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chamados do cliente"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Perfil diferente de CLIENT", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<TicketResponse>> findMyTickets() {

        return ResponseEntity.ok(ticketService.findMyTickets());
    }

    @PreAuthorize("hasAnyRole('CLIENT', 'AGENT', 'ADMIN')")
    @GetMapping("/{id}")
    @Operation(summary = "Consultar chamado", description = "CLIENT acessa apenas chamados próprios. AGENT e ADMIN acessam chamados sem responsável ou atribuídos a si; chamados atribuídos a outra pessoa são negados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chamado encontrado", content = @Content(schema = @Schema(implementation = TicketResponse.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário sem acesso ao chamado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Chamado não encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TicketResponse> findById(@Parameter(description = "ID do chamado", example = "42") @PathVariable Long id) {
        return ResponseEntity.ok(ticketService.findById(id));
    }

    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    @PatchMapping("/{id}/status")
    @Operation(summary = "Alterar status do chamado", description = "Permitido a AGENT e ADMIN, mas somente quando o usuário autenticado é o responsável. Transições aceitas: OPEN → IN_PROGRESS; IN_PROGRESS → WAITING_CLIENT ou RESOLVED; WAITING_CLIENT → IN_PROGRESS ou RESOLVED. CLOSED deve ser confirmado pelo cliente no endpoint de fechamento.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status alterado", content = @Content(schema = @Schema(implementation = TicketResponse.class))),
            @ApiResponse(responseCode = "400", description = "Status ausente ou transição inválida"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão, usuário não responsável ou tentativa de definir CLOSED", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Chamado não encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TicketResponse> updateStatus(@Parameter(description = "ID do chamado", example = "42") @PathVariable Long id,
                                                       @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Novo status do chamado", required = true)
                                                       @RequestBody @Valid UpdateTicketStatusRequest request) {

        return ResponseEntity.ok(ticketService.updateStatus(id, request));
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PatchMapping("/{id}/close")
    @Operation(summary = "Fechar chamado resolvido", description = "O CLIENT criador confirma o fechamento de um chamado que esteja em RESOLVED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chamado fechado", content = @Content(schema = @Schema(implementation = TicketResponse.class))),
            @ApiResponse(responseCode = "400", description = "Chamado não está em RESOLVED", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Perfil diferente de CLIENT ou cliente não é o criador", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Chamado não encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TicketResponse> closeTicket(@Parameter(description = "ID do chamado", example = "42") @PathVariable Long id) {

        return ResponseEntity.ok(ticketService.closeTicket(id));
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/{id}/reject-resolution")
    @Operation(summary = "Rejeitar resolução", description = "O CLIENT criador rejeita, com justificativa obrigatória, a resolução de um chamado RESOLVED. O chamado retorna para IN_PROGRESS e mantém o responsável atual.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Resolução rejeitada", content = @Content(schema = @Schema(implementation = TicketResponse.class))),
            @ApiResponse(responseCode = "400", description = "Justificativa inválida ou chamado não está em RESOLVED", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Perfil diferente de CLIENT ou cliente não é o criador", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Chamado não encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TicketResponse> rejectResolution(
            @Parameter(description = "ID do chamado", example = "42") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Justificativa da rejeição", required = true)
            @RequestBody @Valid RejectResolutionRequest request) {

        return ResponseEntity.ok(ticketService.rejectResolution(id, request));
    }

    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    @PatchMapping("/{id}/assign/me")
    @Operation(summary = "Assumir chamado", description = "AGENT ou ADMIN tenta assumir atomicamente um chamado OPEN e ainda sem responsável. No sucesso, o chamado passa para IN_PROGRESS. A atualização condicional impede que dois usuários assumam o mesmo chamado simultaneamente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Chamado atribuído ao usuário autenticado", content = @Content(schema = @Schema(implementation = TicketResponse.class))),
            @ApiResponse(responseCode = "400", description = "Chamado sem responsável, mas não está em OPEN", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Perfil diferente de AGENT ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Chamado não encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Chamado já atribuído a outro usuário", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<TicketResponse> assignToMe(@Parameter(description = "ID do chamado", example = "42") @PathVariable Long id) {

        return ResponseEntity.ok(ticketService.assignToMe(id));
    }

    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    @GetMapping
    @Operation(summary = "Listar chamados", description = "Lista chamados com filtros e paginação. AGENT vê chamados sem responsável e os atribuídos a si. ADMIN vê todos os chamados. Permitido somente para AGENT e ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de chamados"),
            @ApiResponse(responseCode = "400", description = "Filtro, paginação ou ordenação inválidos"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Perfil diferente de AGENT ou ADMIN", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PageResponse<TicketResponse>> findAll(
            @Parameter(description = "Filtra pelo status") @RequestParam(required = false) TicketStatus status,
            @Parameter(description = "Filtra pela prioridade") @RequestParam(required = false) TicketPriority priority,
            @Parameter(description = "Busca parcial, sem diferenciar maiúsculas, no título ou descrição", example = "impressora") @RequestParam(required = false) String search,
            @Parameter(description = "Índice da página, começando em zero", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de itens por página", example = "10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo e direção separados por vírgula", example = "createdAt,desc") @RequestParam(defaultValue = "createdAt,desc") String sort
            ) {

        String[] sortParams = sort.split(",");

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0])
        );

        return ResponseEntity.ok(ticketService.findAll(status, priority, search, pageable));
    }
}
