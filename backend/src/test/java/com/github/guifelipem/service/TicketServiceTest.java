package com.github.guifelipem.service;

import com.github.guifelipem.dto.common.PageResponse;
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

                assertEquals(
                        "O fechamento do chamado deve ser confirmado pelo cliente",
                        exception.getMessage()
                );
        }

        @Test
        void shouldThrowInvalidTicketStatusTransitionExceptionWhenTransitionIsInvalid() {
                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .status(TicketStatus.OPEN)
                        .build();

                UpdateTicketStatusRequest updateTicketStatusRequest =
                        new UpdateTicketStatusRequest(TicketStatus.WAITING_CLIENT);

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

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
        void shouldThrowTicketNotFoundExceptionWhenAssigningNonExistingTicket() {
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
        }

        @ParameterizedTest
        @EnumSource(
                value = TicketStatus.class,
                names = {"RESOLVED", "CLOSED"}
        )
        void shouldThrowInvalidTicketStatusTransitionExceptionWhenAssigningFinalizedTicket(TicketStatus status) {
                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .status(status)
                        .build();

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                InvalidTicketStatusTransitionException exception = assertThrows(
                        InvalidTicketStatusTransitionException.class,
                        () -> ticketService.assignToMe(1L)
                );

                assertEquals(
                        "Chamado finalizado ou fechado não podem ser atribuídos",
                        exception.getMessage()
                );
        }

        @Test
        void shouldThrowTicketAlreadyAssignedExceptionWhenTicketAlreadyAssigned() {
                User agentAssigned = User.builder()
                        .id(1L)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .status(TicketStatus.OPEN)
                        .assignedTo(agentAssigned)
                        .build();

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
        }

        @ParameterizedTest
        @EnumSource(
                value = TicketStatus.class,
                names = {"OPEN", "IN_PROGRESS", "WAITING_CLIENT"}
        )
        void shouldAssignTicketSuccessfully(TicketStatus status) {
                User agent = User.builder()
                        .id(1L)
                        .build();

                User user = User.builder()
                        .id(2L)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(user)
                        .status(status)
                        .build();

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(agent);

                when(ticketRepository.save(ticket))
                        .thenReturn(ticket);

                TicketResponse response = ticketService.assignToMe(1L);

                // Assert
                ArgumentCaptor<Ticket> ticketCaptor =
                        ArgumentCaptor.forClass(Ticket.class);

                verify(ticketRepository).save(ticketCaptor.capture());

                Ticket ticketToSave = ticketCaptor.getValue();

                assertEquals(TicketStatus.IN_PROGRESS, ticketToSave.getStatus());
                assertEquals(agent, ticketToSave.getAssignedTo());
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

                assertEquals(ticketToSave, statusHistory.getTicket());
                assertEquals(TicketHistoryAction.STATUS_CHANGED, statusHistory.getAction());
                assertEquals(status.name(), statusHistory.getOldValue());
                assertEquals(TicketStatus.IN_PROGRESS.name(), statusHistory.getNewValue());
                assertEquals(agent, statusHistory.getPerformedBy());

                assertEquals(ticketToSave, assignmentHistory.getTicket());
                assertEquals(TicketHistoryAction.TICKET_ASSIGNED, assignmentHistory.getAction());
                assertNull(assignmentHistory.getOldValue());
                assertEquals(ticketToSave.getAssignedTo().getName(), assignmentHistory.getNewValue());
                assertEquals(agent, assignmentHistory.getPerformedBy());

                assertNotNull(statusHistory.getCreatedAt());
                assertNotNull(assignmentHistory.getCreatedAt());
        }

        @Test
        void shouldFindAllSuccessfully() {
                TicketStatus status = TicketStatus.OPEN;
                TicketPriority priority = TicketPriority.LOW;
                String search = "Teste";
                Pageable pageable = PageRequest.of(0, 10);

                User user = User.builder()
                        .id(1L)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(user)
                        .build();

                Page<Ticket> ticketsPage = new PageImpl<>(
                        List.of(ticket),
                        pageable,
                        1
                );

                when(ticketRepository.findAllWithFilters(status, priority, search, pageable))
                        .thenReturn(ticketsPage);

                PageResponse<TicketResponse> response = ticketService.findAll(status, priority, search, pageable);

                verify(ticketRepository).findAllWithFilters(status, priority, search, pageable);

                assertEquals(1, response.content().size());
                assertEquals(1L, response.content().getFirst().id());

                assertEquals(0, response.page());
                assertEquals(10, response.size());
                assertEquals(1, response.totalElements());
                assertEquals(1, response.totalPages());
                assertTrue(response.last());
        }

        @Test
        void shouldTrimSearchBeforeFindingTickets() {
                TicketStatus status = TicketStatus.OPEN;
                TicketPriority priority = TicketPriority.LOW;
                String search = "     Teste    ";
                Pageable pageable = PageRequest.of(0, 10);

                User user = User.builder()
                        .id(1L)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(user)
                        .build();

                Page<Ticket> ticketsPage = new PageImpl<>(
                        List.of(ticket),
                        pageable,
                        1
                );

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

                User user = User.builder()
                        .id(1L)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(user)
                        .build();

                Page<Ticket> ticketsPage = new PageImpl<>(
                        List.of(ticket),
                        pageable,
                        1
                );

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