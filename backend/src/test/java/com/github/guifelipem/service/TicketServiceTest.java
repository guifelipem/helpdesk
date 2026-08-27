package com.github.guifelipem.service;

import com.github.guifelipem.dto.ticket.CreateTicketRequest;
import com.github.guifelipem.dto.ticket.TicketResponse;
import com.github.guifelipem.dto.ticket.UpdateTicketStatusRequest;
import com.github.guifelipem.entity.Ticket;
import com.github.guifelipem.entity.TicketHistory;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.TicketHistoryAction;
import com.github.guifelipem.enums.TicketPriority;
import com.github.guifelipem.enums.TicketStatus;
import com.github.guifelipem.enums.UserRole;
import com.github.guifelipem.exception.ForbiddenException;
import com.github.guifelipem.exception.InvalidTicketStatusTransitionException;
import com.github.guifelipem.exception.TicketNotFoundException;
import com.github.guifelipem.repository.TicketHistoryRepository;
import com.github.guifelipem.repository.TicketRepository;
import com.github.guifelipem.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

        @Mock
        private TicketRepository ticketRepository;

        @Mock
        private AuthenticatedUserProvider authenticatedUserProvider;

        @Mock
        private TicketHistoryRepository ticketHistoryRepository;

        @InjectMocks
        private TicketService ticketService;

        @Test
        void shouldCreateTicketSuccessfully() {
                // Arrange
                User user = User.builder()
                        .id(1L)
                        .name("Guilherme")
                        .email("guilherme@email.com")
                        .build();

                CreateTicketRequest request = new CreateTicketRequest(
                        "Problema no sistema",
                        "Não consigo acessar o sistema",
                        TicketPriority.HIGH
                );

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(user);

                when(ticketRepository.save(any(Ticket.class)))
                        .thenAnswer(invocation -> {
                                Ticket ticket = invocation.getArgument(0);
                                ticket.setId(1L);
                                return ticket;
                        });

                // Act
                TicketResponse response = ticketService.create(request);

                // Assert
                ArgumentCaptor<Ticket> ticketCaptor =
                        ArgumentCaptor.forClass(Ticket.class);

                verify(ticketRepository).save(ticketCaptor.capture());

                Ticket ticketToSave = ticketCaptor.getValue();

                assertEquals(request.title(), ticketToSave.getTitle());
                assertEquals(request.description(), ticketToSave.getDescription());
                assertEquals(request.priority(), ticketToSave.getPriority());
                assertEquals(TicketStatus.OPEN, ticketToSave.getStatus());
                assertEquals(user, ticketToSave.getCreatedBy());
                assertNotNull(ticketToSave.getCreatedAt());
                assertNotNull(ticketToSave.getUpdatedAt());

                assertEquals(1L, response.id());
                assertEquals(request.title(), response.title());
                assertEquals(TicketStatus.OPEN, response.status());

                ArgumentCaptor<TicketHistory> historyCaptor =
                        ArgumentCaptor.forClass(TicketHistory.class);

                verify(ticketHistoryRepository).save(historyCaptor.capture());

                TicketHistory historyToSave = historyCaptor.getValue();

                assertEquals(ticketToSave, historyToSave.getTicket());
                assertEquals(TicketHistoryAction.TICKET_CREATED, historyToSave.getAction());
                assertEquals(TicketStatus.OPEN.name(), historyToSave.getNewValue());
                assertEquals(user, historyToSave.getPerformedBy());
                assertNotNull(historyToSave.getCreatedAt());
        }

        @Test
        void shouldThrowTicketNotFoundExceptionWhenTicketDoesNotExist() {
                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.empty());

                TicketNotFoundException exception = assertThrows(
                        TicketNotFoundException.class,
                        () -> ticketService.findById(1L)
                );

                assertEquals(
                        "Chamado não encontrado",
                        exception.getMessage()
                );
        }

        @Test
        void shouldThrowForbiddenExceptionWhenClientTriesToAccessAnotherUsersTicket() {
                User owner = User.builder()
                        .id(1L)
                        .name("Guilherme")
                        .role(UserRole.CLIENT)
                        .build();

                User anotherClient = User.builder()
                        .id(2L)
                        .name("João")
                        .role(UserRole.CLIENT)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .title("Problema no sistema")
                        .description("Descrição")
                        .priority(TicketPriority.HIGH)
                        .status(TicketStatus.OPEN)
                        .createdBy(owner)
                        .build();

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(anotherClient);

                ForbiddenException exception = assertThrows(
                        ForbiddenException.class,
                        () -> ticketService.findById(1L)
                );

                assertEquals(
                        "Você não tem permissão para visualizar este chamado",
                        exception.getMessage()
                );
        }

        @Test
        void shouldThrowForbiddenExceptionWhenTryingToUpdateStatusToClosed() {
                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .status(TicketStatus.RESOLVED)
                        .build();

                UpdateTicketStatusRequest updateTicketStatusRequest =
                        new UpdateTicketStatusRequest(TicketStatus.CLOSED);

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                ForbiddenException exception = assertThrows(
                        ForbiddenException.class,
                        () -> ticketService.updateStatus(1L, updateTicketStatusRequest)
                );
        }
}