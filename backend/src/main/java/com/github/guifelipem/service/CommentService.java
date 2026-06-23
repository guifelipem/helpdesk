package com.github.guifelipem.service;

import com.github.guifelipem.dto.comment.CommentResponse;
import com.github.guifelipem.dto.comment.CreateCommentRequest;
import com.github.guifelipem.entity.Comment;
import com.github.guifelipem.entity.Ticket;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.Role;
import com.github.guifelipem.exception.TicketNotFoundException;
import com.github.guifelipem.exception.UserNotFoundException;
import com.github.guifelipem.repository.CommentRepository;
import com.github.guifelipem.repository.TicketRepository;
import com.github.guifelipem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponse create(Long ticketId, CreateCommentRequest request) {

        User user = getAuthenticatedUser();

        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow(() ->
                new TicketNotFoundException("Chamado não encontrado"));

        boolean isClient = user.getRole() == Role.CLIENT;
        boolean isOwner = ticket.getCreatedBy().getId().equals(user.getId());

        if (isClient && !isOwner) {
            throw new AccessDeniedException("Você não tem acesso a este chamado");
        }

        if (Boolean.TRUE.equals(request.isInternal()) && isClient) {
            throw new AccessDeniedException("Cliente não pode criar comentário interno");
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

        User user = getAuthenticatedUser();

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Chamado não encontrado"));

        boolean isClient = user.getRole() == Role.CLIENT;
        boolean isOwner = ticket.getCreatedBy().getId().equals(user.getId());

        if (isClient && !isOwner) {
            throw new AccessDeniedException("Você não tem acesso a este chamado");
        }

        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId).stream()
                .filter(comment -> !isClient || !Boolean.TRUE.equals(comment.getIsInternal()))
                .map(this::toResponse)
                .toList();
    }

    private CommentResponse toResponse(Comment comment) {

        return new CommentResponse(
                comment.getId(),
                comment.getMessage(),
                comment.getIsInternal(),
                comment.getUser().getId(),
                comment.getUser().getName(),
                comment.getCreatedAt()
        );
    }

    private User getAuthenticatedUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email).orElseThrow(() ->
                new UserNotFoundException("Usuário não encontrado")
        );
    }
}
