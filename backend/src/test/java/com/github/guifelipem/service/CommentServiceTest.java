package com.github.guifelipem.service;

import com.github.guifelipem.dto.comment.CommentResponse;
import com.github.guifelipem.dto.comment.CreateCommentRequest;
import com.github.guifelipem.entity.Comment;
import com.github.guifelipem.entity.Ticket;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.TicketStatus;
import com.github.guifelipem.enums.UserRole;
import com.github.guifelipem.exception.ForbiddenException;
import com.github.guifelipem.exception.TicketNotFoundException;
import com.github.guifelipem.repository.CommentRepository;
import com.github.guifelipem.repository.TicketRepository;
import com.github.guifelipem.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

        @Mock
        private CommentRepository commentRepository;

        @Mock
        private TicketRepository ticketRepository;

        @Mock
        private AuthenticatedUserProvider authenticatedUserProvider;

        @InjectMocks
        private CommentService commentService;

        @Test
        void shouldCreateCommentSuccessfully() {
                CreateCommentRequest request =
                        new CreateCommentRequest("Teste", null);

                User user = User.builder()
                        .id(1L)
                        .role(UserRole.CLIENT)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(user)
                        .build();

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(user);

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                when(commentRepository.save(any(Comment.class)))
                        .thenAnswer(invocation -> {
                                Comment comment = invocation.getArgument(0);
                                comment.setId(1L);
                                return comment;
                        });

                CommentResponse response = commentService.create(1L, request);

                ArgumentCaptor<Comment> commentCaptor =
                        ArgumentCaptor.forClass(Comment.class);

                verify(commentRepository).save(commentCaptor.capture());

                Comment commentToSave = commentCaptor.getValue();

                assertEquals(request.message(), commentToSave.getMessage());
                assertEquals(request.isInternal(), commentToSave.getIsInternal());
                assertEquals(ticket, commentToSave.getTicket());
                assertEquals(user, commentToSave.getUser());
                assertNotNull(commentToSave.getCreatedAt());

                assertNotNull(ticket.getUpdatedAt());

                assertEquals(1L, response.id());
                assertEquals(request.message(), response.message());
                assertEquals(request.isInternal(), response.isInternal());
                assertEquals(user.getId(), response.author().id());
                assertEquals(user.getRole(), response.author().role());
                assertNotNull(response.createdAt());
        }

        @Test
        void shouldThrowTicketNotFoundExceptionWhenCreatingCommentForNonExistingTicket() {
                CreateCommentRequest request =
                        new CreateCommentRequest("Teste", false);

                User user = User.builder()
                        .id(1L)
                        .role(UserRole.CLIENT)
                        .build();

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(user);

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.empty());

                TicketNotFoundException exception = assertThrows(
                        TicketNotFoundException.class,
                        () -> commentService.create(1L, request)
                );

                assertEquals(
                        "Chamado não encontrado",
                        exception.getMessage()
                );
        }

        @Test
        void shouldThrowForbiddenExceptionWhenClientCommentsOnAnotherUsersTicket() {
                CreateCommentRequest request =
                        new CreateCommentRequest("Teste", false);

                User client = User.builder()
                        .id(1L)
                        .role(UserRole.CLIENT)
                        .build();

                User owner = User.builder()
                        .id(2L)
                        .role(UserRole.CLIENT)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(owner)
                        .build();

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(client);

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                ForbiddenException exception = assertThrows(
                        ForbiddenException.class,
                        () -> commentService.create(1L, request)
                );

                assertEquals(
                        "Você não tem acesso a este chamado",
                        exception.getMessage()
                );
        }

        @Test
        void shouldThrowForbiddenExceptionWhenCommentingOnClosedTicket() {
                CreateCommentRequest request =
                        new CreateCommentRequest("Teste", false);

                User user = User.builder()
                        .id(1L)
                        .role(UserRole.CLIENT)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(user)
                        .status(TicketStatus.CLOSED)
                        .build();

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(user);

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                ForbiddenException exception = assertThrows(
                        ForbiddenException.class,
                        () -> commentService.create(1L, request)
                );

                assertEquals(
                        "Não é possível comentar em um chamado encerrado",
                        exception.getMessage()
                );
        }

        @Test
        void shouldThrowForbiddenExceptionWhenClientCreatesInternalComment() {
                CreateCommentRequest request =
                        new CreateCommentRequest("Comentário interno", true);

                User user = User.builder()
                        .id(1L)
                        .role(UserRole.CLIENT)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(user)
                        .build();

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(user);

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                ForbiddenException exception = assertThrows(
                        ForbiddenException.class,
                        () -> commentService.create(1L, request)
                );

                assertEquals(
                        "Cliente não pode criar comentário interno",
                        exception.getMessage()
                );
        }

        @Test
        void shouldFindCommentsByTicketSuccessfully() {
                User agent = User.builder()
                        .id(1L)
                        .name("Agente")
                        .role(UserRole.AGENT)
                        .build();

                User owner = User.builder()
                        .id(2L)
                        .name("Cliente")
                        .role(UserRole.CLIENT)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(owner)
                        .build();

                Comment comment = Comment.builder()
                        .id(1L)
                        .ticket(ticket)
                        .user(agent)
                        .message("Teste")
                        .isInternal(false)
                        .build();

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(agent);

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                when(commentRepository.findByTicketIdOrderByCreatedAtAsc(1L))
                        .thenReturn(List.of(comment));

                List<CommentResponse> response =
                        commentService.findByTicket(1L);

                assertEquals(1, response.size());
                assertEquals(1L, response.getFirst().id());
                assertEquals("Teste", response.getFirst().message());
                assertEquals(false, response.getFirst().isInternal());
                assertEquals(agent.getId(), response.getFirst().author().id());
        }

        @Test
        void shouldThrowTicketNotFoundExceptionWhenFindingCommentsForNonExistingTicket() {
                User user = User.builder()
                        .id(1L)
                        .role(UserRole.CLIENT)
                        .build();

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(user);

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.empty());

                TicketNotFoundException exception = assertThrows(
                        TicketNotFoundException.class,
                        () -> commentService.findByTicket(1L)
                );

                assertEquals(
                        "Chamado não encontrado",
                        exception.getMessage()
                );
        }

        @Test
        void shouldThrowForbiddenExceptionWhenClientFindsCommentsFromAnotherUsersTicket() {
                User client = User.builder()
                        .id(1L)
                        .role(UserRole.CLIENT)
                        .build();

                User owner = User.builder()
                        .id(2L)
                        .role(UserRole.CLIENT)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(owner)
                        .build();

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(client);

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                ForbiddenException exception = assertThrows(
                        ForbiddenException.class,
                        () -> commentService.findByTicket(1L)
                );

                assertEquals(
                        "Você não tem acesso a este chamado",
                        exception.getMessage()
                );
        }

        @Test
        void shouldHideInternalCommentsFromClient() {
                User client = User.builder()
                        .id(1L)
                        .name("Cliente")
                        .role(UserRole.CLIENT)
                        .build();

                User agent = User.builder()
                        .id(2L)
                        .name("Agente")
                        .role(UserRole.AGENT)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(client)
                        .build();

                Comment publicComment = Comment.builder()
                        .id(1L)
                        .ticket(ticket)
                        .user(agent)
                        .message("Comentário público")
                        .isInternal(false)
                        .build();

                Comment internalComment = Comment.builder()
                        .id(2L)
                        .ticket(ticket)
                        .user(agent)
                        .message("Comentário interno")
                        .isInternal(true)
                        .build();

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(client);

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                when(commentRepository.findByTicketIdOrderByCreatedAtAsc(1L))
                        .thenReturn(List.of(publicComment, internalComment));

                List<CommentResponse> response =
                        commentService.findByTicket(1L);

                assertEquals(1, response.size());
                assertEquals("Comentário público", response.getFirst().message());
                assertEquals(false, response.getFirst().isInternal());
        }

        @Test
        void shouldAllowAgentToSeeInternalComments() {
                User agent = User.builder()
                        .id(1L)
                        .name("Agente")
                        .role(UserRole.AGENT)
                        .build();

                User owner = User.builder()
                        .id(2L)
                        .name("Cliente")
                        .role(UserRole.CLIENT)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(owner)
                        .build();

                Comment publicComment = Comment.builder()
                        .id(1L)
                        .ticket(ticket)
                        .user(agent)
                        .message("Comentário público")
                        .isInternal(false)
                        .build();

                Comment internalComment = Comment.builder()
                        .id(2L)
                        .ticket(ticket)
                        .user(agent)
                        .message("Comentário interno")
                        .isInternal(true)
                        .build();

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(agent);

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                when(commentRepository.findByTicketIdOrderByCreatedAtAsc(1L))
                        .thenReturn(List.of(publicComment, internalComment));

                List<CommentResponse> response =
                        commentService.findByTicket(1L);

                assertEquals(2, response.size());
                assertEquals("Comentário público", response.get(0).message());
                assertEquals("Comentário interno", response.get(1).message());
                assertEquals(true, response.get(1).isInternal());
        }

        @Test
        void shouldRejectCommentByAgentWhoIsNotResponsible() {
                User responsible = User.builder().id(1L).role(UserRole.AGENT).build();
                User anotherAgent = User.builder().id(2L).role(UserRole.AGENT).build();
                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(User.builder().id(3L).role(UserRole.CLIENT).build())
                        .assignedTo(responsible)
                        .build();

                when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(anotherAgent);
                when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

                ForbiddenException exception = assertThrows(
                        ForbiddenException.class,
                        () -> commentService.create(1L, new CreateCommentRequest("Teste", true))
                );

                assertEquals("Somente o responsável pode comentar neste chamado", exception.getMessage());
        }

        @Test
        void shouldRejectCommentsForAgentWhoIsNotResponsible() {
                User responsible = User.builder().id(1L).role(UserRole.AGENT).build();
                User anotherAgent = User.builder().id(2L).role(UserRole.AGENT).build();
                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(User.builder().id(3L).role(UserRole.CLIENT).build())
                        .assignedTo(responsible)
                        .build();

                when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(anotherAgent);
                when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));

                ForbiddenException exception = assertThrows(
                        ForbiddenException.class,
                        () -> commentService.findByTicket(1L)
                );

                assertEquals(
                        "Somente o responsável pode acessar os comentários deste chamado",
                        exception.getMessage()
                );
        }
}
