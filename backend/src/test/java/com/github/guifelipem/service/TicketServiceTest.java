package com.github.guifelipem.service;

import com.github.guifelipem.dto.common.PageResponse;
import com.github.guifelipem.dto.ticket.CreateTicketRequest;
import com.github.guifelipem.dto.ticket.RejectResolutionRequest;
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
import com.github.guifelipem.exception.TicketAlreadyAssignedException;
import com.github.guifelipem.exception.TicketNotFoundException;
import com.github.guifelipem.repository.TicketHistoryRepository;
import com.github.guifelipem.repository.TicketRepository;
import com.github.guifelipem.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        void shouldThrowTicketNotFoundExceptionWhenUpdatingStatusNonExistingTicket() {
                UpdateTicketStatusRequest updateTicketStatusRequest =
                        new UpdateTicketStatusRequest(TicketStatus.WAITING_CLIENT);

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.empty());

                TicketNotFoundException exception = assertThrows(
                        TicketNotFoundException.class,
                        () -> ticketService.updateStatus(1L, updateTicketStatusRequest)
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
                        .build();

                User anotherClient = User.builder()
                        .id(2L)
                        .role(UserRole.CLIENT)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
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
                User agent = User.builder()
                        .id(1L)
                        .role(UserRole.AGENT)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .status(TicketStatus.RESOLVED)
                        .assignedTo(agent)
                        .build();

                UpdateTicketStatusRequest updateTicketStatusRequest =
                        new UpdateTicketStatusRequest(TicketStatus.CLOSED);

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(agent);

                ForbiddenException exception = assertThrows(
                        ForbiddenException.class,
                        () -> ticketService.updateStatus(1L, updateTicketStatusRequest)
                );

                assertEquals(
                        "O fechamento do chamado deve ser confirmado pelo cliente",
                        exception.getMessage()
                );
        }

        @Test
        void shouldThrowInvalidTicketStatusTransitionExceptionWhenTransitionIsInvalid() {
                User agent = User.builder()
                        .id(1L)
                        .role(UserRole.AGENT)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .status(TicketStatus.OPEN)
                        .assignedTo(agent)
                        .build();

                UpdateTicketStatusRequest updateTicketStatusRequest =
                        new UpdateTicketStatusRequest(TicketStatus.WAITING_CLIENT);

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(agent);

                InvalidTicketStatusTransitionException exception = assertThrows(
                        InvalidTicketStatusTransitionException.class,
                        () -> ticketService.updateStatus(1L, updateTicketStatusRequest)
                );

                TicketStatus newStatus = updateTicketStatusRequest.status();

                assertEquals(
                        "Transição de status inválida: " + ticket.getStatus() + " -> " + newStatus,
                        exception.getMessage()
                );
        }

        @Test
        void shouldUpdateTicketStatusSuccessfully() {
                User user = User.builder()
                        .id(1L)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .status(TicketStatus.WAITING_CLIENT)
                        .createdBy(user)
                        .assignedTo(user)
                        .build();

                UpdateTicketStatusRequest request =
                        new UpdateTicketStatusRequest(TicketStatus.IN_PROGRESS);

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(user);

                when(ticketRepository.save(ticket))
                        .thenReturn(ticket);

                TicketResponse response = ticketService.updateStatus(1L, request);

                // Assert
                ArgumentCaptor<Ticket> ticketCaptor =
                        ArgumentCaptor.forClass(Ticket.class);

                verify(ticketRepository).save(ticketCaptor.capture());

                Ticket ticketToSave = ticketCaptor.getValue();

                assertEquals(request.status(), ticketToSave.getStatus());

                assertEquals(1L, response.id());

                ArgumentCaptor<TicketHistory> historyCaptor =
                        ArgumentCaptor.forClass(TicketHistory.class);

                verify(ticketHistoryRepository).save(historyCaptor.capture());

                TicketHistory historyToSave = historyCaptor.getValue();

                assertEquals(ticketToSave, historyToSave.getTicket());
                assertEquals(TicketHistoryAction.STATUS_CHANGED, historyToSave.getAction());
                assertEquals(TicketStatus.WAITING_CLIENT.name(), historyToSave.getOldValue());
                assertEquals(TicketStatus.IN_PROGRESS.name(), historyToSave.getNewValue());
                assertEquals(user, historyToSave.getPerformedBy());
                assertNotNull(historyToSave.getCreatedAt());
        }

        @ParameterizedTest
        @EnumSource(value = UserRole.class, names = {"AGENT", "ADMIN"})
        void shouldRejectStatusUpdateByNonResponsibleStaff(UserRole role) {
                User responsible = User.builder().id(1L).role(UserRole.AGENT).build();
                User currentUser = User.builder().id(2L).role(role).build();
                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .status(TicketStatus.IN_PROGRESS)
                        .assignedTo(responsible)
                        .build();

                when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
                when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(currentUser);

                ForbiddenException exception = assertThrows(
                        ForbiddenException.class,
                        () -> ticketService.updateStatus(
                                1L,
                                new UpdateTicketStatusRequest(TicketStatus.WAITING_CLIENT)
                        )
                );

                assertEquals("Somente o responsável pode alterar o status deste chamado", exception.getMessage());
                verify(ticketRepository, never()).save(any(Ticket.class));
        }

        @Test
        void shouldRejectTicketDetailsForAgentWhoIsNotResponsible() {
                User responsible = User.builder().id(1L).role(UserRole.AGENT).build();
                User anotherAgent = User.builder().id(2L).role(UserRole.AGENT).build();
                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(User.builder().id(3L).build())
                        .assignedTo(responsible)
                        .build();

                when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
                when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(anotherAgent);

                ForbiddenException exception = assertThrows(
                        ForbiddenException.class,
                        () -> ticketService.findById(1L)
                );

                assertEquals("Somente o responsável pode visualizar este chamado", exception.getMessage());
        }

        @Test
        void shouldThrowTicketNotFoundExceptionWhenClosingNonExistingTicket() {
                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.empty());

                TicketNotFoundException exception = assertThrows(
                        TicketNotFoundException.class,
                        () -> ticketService.closeTicket(1L)
                );

                assertEquals(
                        "Chamado não encontrado",
                        exception.getMessage()
                );
        }

        @Test
        void shouldThrowForbiddenExceptionWhenClientTriesToCloseAnotherUsersTicket() {
                User owner = User.builder()
                        .id(1L)
                        .build();

                User anotherClient = User.builder()
                        .id(2L)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(owner)
                        .status(TicketStatus.RESOLVED)
                        .build();

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(anotherClient);

                ForbiddenException exception = assertThrows(
                        ForbiddenException.class,
                        () -> ticketService.closeTicket(1L)
                );

                assertEquals(
                        "Você não tem permissão para fechar este chamado",
                        exception.getMessage()
                );
        }

        @Test
        void shouldThrowInvalidTicketStatusTransitionExceptionWhenClosingTicketThatIsNotResolved() {
                User user = User.builder()
                        .id(1L)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .status(TicketStatus.OPEN)
                        .createdBy(user)
                        .build();

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(user);

                InvalidTicketStatusTransitionException exception = assertThrows(
                        InvalidTicketStatusTransitionException.class,
                        () -> ticketService.closeTicket(1L)
                );

                assertEquals(
                        "Apenas chamados resolvidos podem ser fechados",
                        exception.getMessage()
                );
        }

        @Test
        void shouldCloseTicketSuccessfully() {
                User user = User.builder()
                        .id(1L)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .status(TicketStatus.RESOLVED)
                        .createdBy(user)
                        .build();

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(user);

                when(ticketRepository.save(ticket))
                        .thenReturn(ticket);

                TicketResponse response = ticketService.closeTicket(1L);

                // Assert
                ArgumentCaptor<Ticket> ticketCaptor =
                        ArgumentCaptor.forClass(Ticket.class);

                verify(ticketRepository).save(ticketCaptor.capture());

                Ticket ticketToSave = ticketCaptor.getValue();

                assertEquals(TicketStatus.CLOSED, ticketToSave.getStatus());
                assertEquals(1L, response.id());
                assertEquals(TicketStatus.CLOSED, response.status());

                ArgumentCaptor<TicketHistory> historyCaptor =
                        ArgumentCaptor.forClass(TicketHistory.class);

                verify(ticketHistoryRepository).save(historyCaptor.capture());

                TicketHistory historyToSave = historyCaptor.getValue();

                assertEquals(ticketToSave, historyToSave.getTicket());
                assertEquals(TicketHistoryAction.STATUS_CHANGED, historyToSave.getAction());
                assertEquals(TicketStatus.RESOLVED.name(), historyToSave.getOldValue());
                assertEquals(TicketStatus.CLOSED.name(), historyToSave.getNewValue());
                assertEquals(user, historyToSave.getPerformedBy());
                assertNotNull(historyToSave.getCreatedAt());
        }

        @Test
        void shouldRejectResolutionSuccessfully() {
                User client = User.builder().id(1L).name("Cliente").role(UserRole.CLIENT).build();
                User agent = User.builder().id(2L).name("Agente").role(UserRole.AGENT).build();
                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .status(TicketStatus.RESOLVED)
                        .createdBy(client)
                        .assignedTo(agent)
                        .build();
                RejectResolutionRequest request = new RejectResolutionRequest("O problema ainda acontece.");

                when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
                when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(client);
                when(ticketRepository.save(ticket)).thenReturn(ticket);

                TicketResponse response = ticketService.rejectResolution(1L, request);

                assertEquals(TicketStatus.IN_PROGRESS, response.status());
                assertEquals(agent.getId(), response.assignedTo().id());
                assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus());
                assertSame(agent, ticket.getAssignedTo());
                assertNotNull(ticket.getUpdatedAt());

                ArgumentCaptor<TicketHistory> historyCaptor = ArgumentCaptor.forClass(TicketHistory.class);
                verify(ticketHistoryRepository).save(historyCaptor.capture());

                TicketHistory history = historyCaptor.getValue();
                assertEquals(TicketHistoryAction.RESOLUTION_REJECTED, history.getAction());
                assertEquals(TicketStatus.RESOLVED.name(), history.getOldValue());
                assertEquals(TicketStatus.IN_PROGRESS.name(), history.getNewValue());
                assertEquals(request.reason(), history.getDetails());
                assertSame(client, history.getPerformedBy());
                assertNotNull(history.getCreatedAt());
        }

        @Test
        void shouldRejectResolutionRejectionByClientWhoDoesNotOwnTicket() {
                User owner = User.builder().id(1L).role(UserRole.CLIENT).build();
                User anotherClient = User.builder().id(2L).role(UserRole.CLIENT).build();
                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .status(TicketStatus.RESOLVED)
                        .createdBy(owner)
                        .build();

                when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
                when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(anotherClient);

                ForbiddenException exception = assertThrows(
                        ForbiddenException.class,
                        () -> ticketService.rejectResolution(1L, new RejectResolutionRequest("Ainda falha"))
                );

                assertEquals("Somente o cliente dono do chamado pode rejeitar a resolução", exception.getMessage());
                verify(ticketRepository, never()).save(any(Ticket.class));
                verify(ticketHistoryRepository, never()).save(any(TicketHistory.class));
        }

        @Test
        void shouldRejectResolutionRejectionByNonClientEvenWhenTicketOwner() {
                User agent = User.builder().id(1L).role(UserRole.AGENT).build();
                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .status(TicketStatus.RESOLVED)
                        .createdBy(agent)
                        .build();

                when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
                when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(agent);

                assertThrows(
                        ForbiddenException.class,
                        () -> ticketService.rejectResolution(1L, new RejectResolutionRequest("Ainda falha"))
                );

                verify(ticketRepository, never()).save(any(Ticket.class));
        }

        @Test
        void shouldRejectResolutionRejectionWhenTicketIsNotResolved() {
                User client = User.builder().id(1L).role(UserRole.CLIENT).build();
                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .status(TicketStatus.IN_PROGRESS)
                        .createdBy(client)
                        .build();

                when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
                when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(client);

                InvalidTicketStatusTransitionException exception = assertThrows(
                        InvalidTicketStatusTransitionException.class,
                        () -> ticketService.rejectResolution(1L, new RejectResolutionRequest("Ainda falha"))
                );

                assertEquals("Apenas chamados resolvidos podem ter a resolução rejeitada", exception.getMessage());
                verify(ticketRepository, never()).save(any(Ticket.class));
        }

        @Test
        void shouldNotReopenClosedTicketThroughResolutionRejection() {
                User client = User.builder().id(1L).role(UserRole.CLIENT).build();
                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .status(TicketStatus.CLOSED)
                        .createdBy(client)
                        .build();

                when(ticketRepository.findById(1L)).thenReturn(Optional.of(ticket));
                when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(client);

                assertThrows(
                        InvalidTicketStatusTransitionException.class,
                        () -> ticketService.rejectResolution(1L, new RejectResolutionRequest("Ainda falha"))
                );

                assertEquals(TicketStatus.CLOSED, ticket.getStatus());
                verify(ticketRepository, never()).save(any(Ticket.class));
                verify(ticketHistoryRepository, never()).save(any(TicketHistory.class));
        }

        @Test
        void shouldThrowTicketNotFoundExceptionWhenAssigningNonExistingTicket() {
                User agent = User.builder().id(1L).build();

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(agent);
                when(ticketRepository.assignIfAvailable(
                        eq(1L),
                        eq(agent),
                        eq(TicketStatus.OPEN),
                        eq(TicketStatus.IN_PROGRESS),
                        any(LocalDateTime.class)
                )).thenReturn(0);
                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.empty());

                TicketNotFoundException exception = assertThrows(
                        TicketNotFoundException.class,
                        () -> ticketService.assignToMe(1L)
                );

                assertEquals(
                        "Chamado não encontrado",
                        exception.getMessage()
                );

                verify(ticketHistoryRepository, never()).save(any(TicketHistory.class));
        }

        @ParameterizedTest
        @EnumSource(
                value = TicketStatus.class,
                names = {"IN_PROGRESS", "WAITING_CLIENT", "RESOLVED", "CLOSED"}
        )
        void shouldThrowInvalidTicketStatusTransitionExceptionWhenAssigningNonOpenTicket(TicketStatus status) {
                User agent = User.builder().id(1L).build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .status(status)
                        .build();

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(agent);
                when(ticketRepository.assignIfAvailable(
                        eq(1L),
                        eq(agent),
                        eq(TicketStatus.OPEN),
                        eq(TicketStatus.IN_PROGRESS),
                        any(LocalDateTime.class)
                )).thenReturn(0);
                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                InvalidTicketStatusTransitionException exception = assertThrows(
                        InvalidTicketStatusTransitionException.class,
                        () -> ticketService.assignToMe(1L)
                );

                assertEquals(
                        "Apenas chamados em aberto podem ser atribuídos",
                        exception.getMessage()
                );

                verify(ticketHistoryRepository, never()).save(any(TicketHistory.class));
        }

        @Test
        void shouldThrowTicketAlreadyAssignedExceptionWhenTicketAlreadyAssigned() {
                User agent = User.builder().id(2L).build();

                User agentAssigned = User.builder()
                        .id(1L)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .status(TicketStatus.OPEN)
                        .assignedTo(agentAssigned)
                        .build();

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(agent);
                when(ticketRepository.assignIfAvailable(
                        eq(1L),
                        eq(agent),
                        eq(TicketStatus.OPEN),
                        eq(TicketStatus.IN_PROGRESS),
                        any(LocalDateTime.class)
                )).thenReturn(0);
                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                TicketAlreadyAssignedException exception = assertThrows(
                        TicketAlreadyAssignedException.class,
                        () -> ticketService.assignToMe(1L)
                );

                assertEquals(
                        "Chamado já está atribuído a um agente",
                        exception.getMessage()
                );

                verify(ticketHistoryRepository, never()).save(any(TicketHistory.class));
        }

        @Test
        void shouldAssignTicketSuccessfully() {
                User agent = User.builder()
                        .id(1L)
                        .name("Agente")
                        .build();

                User user = User.builder()
                        .id(2L)
                        .build();

                Ticket updatedTicket = Ticket.builder()
                        .id(1L)
                        .createdBy(user)
                        .assignedTo(agent)
                        .status(TicketStatus.IN_PROGRESS)
                        .build();

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(agent);

                when(ticketRepository.assignIfAvailable(
                        eq(1L),
                        eq(agent),
                        eq(TicketStatus.OPEN),
                        eq(TicketStatus.IN_PROGRESS),
                        any(LocalDateTime.class)
                )).thenReturn(1);

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(updatedTicket));

                TicketResponse response = ticketService.assignToMe(1L);

                // Assert
                verify(ticketRepository).assignIfAvailable(
                        eq(1L),
                        eq(agent),
                        eq(TicketStatus.OPEN),
                        eq(TicketStatus.IN_PROGRESS),
                        any(LocalDateTime.class)
                );
                verify(ticketRepository, never()).save(any(Ticket.class));

                assertEquals(1L, response.id());
                assertEquals(TicketStatus.IN_PROGRESS, response.status());
                assertEquals(agent.getId(), response.assignedTo().id());

                ArgumentCaptor<TicketHistory> historyCaptor =
                        ArgumentCaptor.forClass(TicketHistory.class);

                verify(ticketHistoryRepository, times(2))
                        .save(historyCaptor.capture());

                List<TicketHistory> histories = historyCaptor.getAllValues();

                TicketHistory statusHistory = histories.get(0);
                TicketHistory assignmentHistory = histories.get(1);

                assertEquals(updatedTicket, statusHistory.getTicket());
                assertEquals(TicketHistoryAction.STATUS_CHANGED, statusHistory.getAction());
                assertEquals(TicketStatus.OPEN.name(), statusHistory.getOldValue());
                assertEquals(TicketStatus.IN_PROGRESS.name(), statusHistory.getNewValue());
                assertEquals(agent, statusHistory.getPerformedBy());

                assertEquals(updatedTicket, assignmentHistory.getTicket());
                assertEquals(TicketHistoryAction.TICKET_ASSIGNED, assignmentHistory.getAction());
                assertNull(assignmentHistory.getOldValue());
                assertEquals(agent.getName(), assignmentHistory.getNewValue());
                assertEquals(agent, assignmentHistory.getPerformedBy());

                assertNotNull(statusHistory.getCreatedAt());
                assertNotNull(assignmentHistory.getCreatedAt());
        }

        @Test
        void shouldFindAllTicketsAsAdminSuccessfully() {
                TicketStatus status = TicketStatus.OPEN;
                TicketPriority priority = TicketPriority.LOW;
                String search = "Teste";
                Pageable pageable = PageRequest.of(0, 10);

                User admin = User.builder()
                        .id(1L)
                        .role(UserRole.ADMIN)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(admin)
                        .build();

                Page<Ticket> ticketsPage = new PageImpl<>(
                        List.of(ticket),
                        pageable,
                        1
                );

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(admin);

                when(ticketRepository.findAllWithFilters(status, priority, search, pageable))
                        .thenReturn(ticketsPage);

                PageResponse<TicketResponse> response = ticketService.findAll(status, priority, search, pageable);

                verify(ticketRepository).findAllWithFilters(status, priority, search, pageable);
                verify(ticketRepository, never()).findAllVisibleToAgentWithFilters(any(), any(), any(), any(), any());

                assertEquals(1, response.content().size());
                assertEquals(1L, response.content().getFirst().id());

                assertEquals(0, response.page());
                assertEquals(10, response.size());
                assertEquals(1, response.totalElements());
                assertEquals(1, response.totalPages());
                assertTrue(response.last());
        }

        @Test
        void shouldFindOnlyTicketsVisibleToAuthenticatedAgent() {
                TicketStatus status = TicketStatus.IN_PROGRESS;
                TicketPriority priority = TicketPriority.HIGH;
                String search = "  Sistema  ";
                Pageable pageable = PageRequest.of(0, 10);

                User agent = User.builder()
                        .id(2L)
                        .role(UserRole.AGENT)
                        .build();

                User client = User.builder()
                        .id(1L)
                        .role(UserRole.CLIENT)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(client)
                        .assignedTo(agent)
                        .build();

                Page<Ticket> ticketsPage = new PageImpl<>(
                        List.of(ticket),
                        pageable,
                        1
                );

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(agent);

                when(ticketRepository.findAllVisibleToAgentWithFilters(
                        agent.getId(), status, priority, "Sistema", pageable
                )).thenReturn(ticketsPage);

                PageResponse<TicketResponse> response = ticketService.findAll(
                        status, priority, search, pageable
                );

                verify(ticketRepository).findAllVisibleToAgentWithFilters(
                        agent.getId(), status, priority, "Sistema", pageable
                );
                verify(ticketRepository, never()).findAllWithFilters(any(), any(), any(), any());

                assertEquals(1, response.content().size());
                assertEquals(ticket.getId(), response.content().getFirst().id());
                assertEquals(agent.getId(), response.content().getFirst().assignedTo().id());
        }

        @Test
        void shouldTrimSearchBeforeFindingTickets() {
                TicketStatus status = TicketStatus.OPEN;
                TicketPriority priority = TicketPriority.LOW;
                String search = "     Teste    ";
                Pageable pageable = PageRequest.of(0, 10);

                User admin = User.builder()
                        .id(1L)
                        .role(UserRole.ADMIN)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(admin)
                        .build();

                Page<Ticket> ticketsPage = new PageImpl<>(
                        List.of(ticket),
                        pageable,
                        1
                );

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(admin);

                when(ticketRepository.findAllWithFilters(status, priority, "Teste", pageable))
                        .thenReturn(ticketsPage);

                ticketService.findAll(status, priority, search, pageable);

                verify(ticketRepository).findAllWithFilters(status, priority, "Teste", pageable);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"     "})
        void shouldPassNullToRepositoryWhenSearchIsBlank(String search) {
                TicketStatus status = TicketStatus.OPEN;
                TicketPriority priority = TicketPriority.LOW;
                Pageable pageable = PageRequest.of(0, 10);

                User admin = User.builder()
                        .id(1L)
                        .role(UserRole.ADMIN)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(admin)
                        .build();

                Page<Ticket> ticketsPage = new PageImpl<>(
                        List.of(ticket),
                        pageable,
                        1
                );

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(admin);

                when(ticketRepository.findAllWithFilters(status, priority, null, pageable))
                        .thenReturn(ticketsPage);

                ticketService.findAll(status, priority, search, pageable);

                verify(ticketRepository).findAllWithFilters(status, priority, null, pageable);
        }

        @Test
        void shouldFindMyTicketsSuccessfully() {
                User user = User.builder()
                        .id(1L)
                        .build();

                Ticket ticket1 = Ticket.builder()
                        .id(1L)
                        .createdBy(user)
                        .build();

                Ticket ticket2 = Ticket.builder()
                        .id(2L)
                        .createdBy(user)
                        .build();

                List<Ticket> meusTickets= List.of(ticket1, ticket2);

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(user);

                when(ticketRepository.findByCreatedBy(user))
                        .thenReturn(meusTickets);

                List<TicketResponse> response = ticketService.findMyTickets();

                verify(ticketRepository).findByCreatedBy(user);

                assertEquals(2, response.size());

                assertEquals(1L, response.getFirst().id());
                assertEquals(2L, response.get(1).id());
                assertEquals(user.getId(), response.getFirst().createdBy().id());
        }

        @Test
        void shouldReturnEmptyListWhenUserHasNoTickets() {
                User user = User.builder()
                        .id(1L)
                        .build();

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(user);

                when(ticketRepository.findByCreatedBy(user))
                        .thenReturn(List.of());

                List<TicketResponse> response = ticketService.findMyTickets();

                verify(ticketRepository).findByCreatedBy(user);

                assertTrue(response.isEmpty());
        }

        @Test
        void shouldFindTicketByIdAsClientSuccessfully() {
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

                TicketResponse response = ticketService.findById(1L);

                verify(ticketRepository).findById(1L);

                assertEquals(1L, response.id());
                assertEquals(user.getId(), response.createdBy().id());
        }

        @Test
        void shouldAllowAgentToAccessAnotherUsersTicket() {
                User agent = User.builder()
                        .id(2L)
                        .role(UserRole.AGENT)
                        .build();

                User user = User.builder()
                        .id(1L)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(user)
                        .build();

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(agent);

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                TicketResponse response = ticketService.findById(1L);

                verify(ticketRepository).findById(1L);

                assertEquals(1L, response.id());
                assertEquals(user.getId(), response.createdBy().id());
        }
}
