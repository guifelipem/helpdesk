package com.github.guifelipem.service;

import com.github.guifelipem.dto.comment.CommentResponse;
import com.github.guifelipem.dto.comment.CreateCommentRequest;
import com.github.guifelipem.dto.ticket.UserSummaryResponse;
import com.github.guifelipem.entity.Comment;
import com.github.guifelipem.entity.Ticket;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.TicketStatus;
import com.github.guifelipem.enums.UserRole;
import com.github.guifelipem.exception.TicketNotFoundException;
import com.github.guifelipem.repository.CommentRepository;
import com.github.guifelipem.repository.TicketRepository;
import com.github.guifelipem.security.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import com.github.guifelipem.exception.ForbiddenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    @Transactional
    public CommentResponse create(Long ticketId, CreateCommentRequest request) {

        User user = authenticatedUserProvider.getAuthenticatedUser();

        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() ->
                new TicketNotFoundException("Chamado não encontrado"));

        boolean isClient = user.getRole() == UserRole.CLIENT;
        boolean isOwner = ticket.getCreatedBy().getId().equals(user.getId());

        if (isClient && !isOwner) {
            throw new ForbiddenException("Você não tem acesso a este chamado");
        }

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new ForbiddenException("Não é possível comentar em um chamado encerrado");
        }

        if (Boolean.TRUE.equals(request.isInternal()) && isClient) {
            throw new ForbiddenException("Cliente não pode criar comentário interno");
        }

        Comment comment = Comment.builder()
                .ticket(ticket)
                .user(user)
                .message(request.message())
                .isInternal(request.isInternal())
                .createdAt(LocalDateTime.now())
                .build();

        ticket.setUpdatedAt(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);

        return toResponse(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> findByTicket(Long ticketId) {

        User user = authenticatedUserProvider.getAuthenticatedUser();

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Chamado não encontrado"));

        boolean isClient = user.getRole() == UserRole.CLIENT;
        boolean isOwner = ticket.getCreatedBy().getId().equals(user.getId());

        if (isClient && !isOwner) {
            throw new ForbiddenException("Você não tem acesso a este chamado");
        }

        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .filter(comment -> !isClient || !Boolean.TRUE.equals(comment.getIsInternal()))
                .map(this::toResponse)
                .toList();
    }

    private CommentResponse toResponse(Comment comment) {

        UserSummaryResponse author = new UserSummaryResponse(
                comment.getUser().getId(),
                comment.getUser().getName()
        );

        return new CommentResponse(
                comment.getId(),
                comment.getMessage(),
                comment.getIsInternal(),
                author,
                comment.getCreatedAt()
        );
    }

}
