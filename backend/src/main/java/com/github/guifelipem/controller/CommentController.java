package com.github.guifelipem.controller;

import com.github.guifelipem.dto.comment.CommentResponse;
import com.github.guifelipem.dto.comment.CreateCommentRequest;
import com.github.guifelipem.exception.ErrorResponse;
import com.github.guifelipem.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
@RequiredArgsConstructor
@Tag(name = "Comentários", description = "Comentários públicos e internos dos chamados")
public class CommentController {

    private final CommentService commentService;

    @PreAuthorize("hasAnyRole('CLIENT', 'AGENT', 'ADMIN')")
    @PostMapping
    @Operation(summary = "Adicionar comentário", description = "CLIENT pode comentar apenas em chamado próprio e não pode criar comentário interno. AGENT e ADMIN só podem comentar quando forem o responsável. Chamados CLOSED não aceitam comentários.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comentário criado", content = @Content(schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados do comentário inválidos"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Sem acesso, usuário não responsável, comentário interno por cliente ou chamado fechado", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Chamado não encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<CommentResponse> create(@Parameter(description = "ID do chamado", example = "42") @PathVariable Long ticketId,
                                                  @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Mensagem e indicação de visibilidade interna", required = true)
                                                  @RequestBody @Valid CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.create(ticketId, request));
    }

    @PreAuthorize("hasAnyRole('CLIENT', 'AGENT', 'ADMIN')")
    @GetMapping
    @Operation(summary = "Listar comentários", description = "Retorna comentários em ordem cronológica. CLIENT acessa apenas chamado próprio e não recebe comentários internos. AGENT e ADMIN podem acessar chamado sem responsável ou atribuído a si, mas não um chamado atribuído a outra pessoa.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comentários do chamado"),
            @ApiResponse(responseCode = "401", description = "Autenticação necessária", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Usuário sem acesso aos comentários", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Chamado não encontrado", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<List<CommentResponse>> findByTicket(@Parameter(description = "ID do chamado", example = "42") @PathVariable Long ticketId) {
        return ResponseEntity.ok(commentService.findByTicket(ticketId));
    }
}
