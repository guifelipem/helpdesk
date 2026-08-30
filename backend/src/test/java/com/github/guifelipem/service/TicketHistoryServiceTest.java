package com.github.guifelipem.service;

import com.github.guifelipem.dto.history.TicketHistoryResponse;
import com.github.guifelipem.entity.Ticket;
import com.github.guifelipem.entity.TicketHistory;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.TicketHistoryAction;
import com.github.guifelipem.enums.UserRole;
import com.github.guifelipem.exception.ForbiddenException;
import com.github.guifelipem.exception.TicketNotFoundException;
import com.github.guifelipem.repository.TicketHistoryRepository;
import com.github.guifelipem.repository.TicketRepository;
import com.github.guifelipem.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketHistoryServiceTest {

        @Mock
        private TicketHistoryRepository ticketHistoryRepository;

        @Mock
        private AuthenticatedUserProvider authenticatedUserProvider;

        @Mock
        private TicketRepository ticketRepository;

        @InjectMocks
        private TicketHistoryService ticketHistoryService;

        @Test
        void shouldFindTicketHistorySuccessfully() {
                User client = User.builder()
                        .id(1L)
                        .name("Fulano")
                        .role(UserRole.CLIENT)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(client)
                        .build();

                LocalDateTime createdAt = LocalDateTime.now();

                TicketHistory history = TicketHistory.builder()
                        .id(1L)
                        .ticket(ticket)
                        .action(TicketHistoryAction.STATUS_CHANGED)
                        .oldValue("OPEN")
                        .newValue("IN_PROGRESS")
                        .performedBy(client)
                        .createdAt(createdAt)
                        .build();

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(client);

                when(ticketHistoryRepository.findByTicketIdOrderByCreatedAtAsc(1L))
                        .thenReturn(List.of(history));

                List<TicketHistoryResponse> response =
                        ticketHistoryService.findByTicket(1L);

                assertEquals(1, response.size());

                TicketHistoryResponse historyResponse = response.getFirst();

                assertEquals(1L, historyResponse.id());
                assertEquals(TicketHistoryAction.STATUS_CHANGED, historyResponse.action());
                assertEquals("OPEN", historyResponse.oldValue());
                assertEquals("IN_PROGRESS", historyResponse.newValue());
                assertEquals(client.getId(), historyResponse.performedBy().id());
                assertEquals(client.getName(), historyResponse.performedBy().name());
                assertEquals(client.getRole(), historyResponse.performedBy().role());
                assertEquals(createdAt, historyResponse.createdAt());
        }

        @Test
        void shouldThrowTicketNotFoundExceptionWhenTicketDoesNotExist() {
                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.empty());

                TicketNotFoundException exception = assertThrows(
                        TicketNotFoundException.class,
                        () -> ticketHistoryService.findByTicket(1L)
                );

                assertEquals(
                        "Chamado não encontrado",
                        exception.getMessage()
                );
        }

        @Test
        void shouldThrowForbiddenExceptionWhenClientAccessesAnotherUsersTicketHistory() {
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

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(client);

                ForbiddenException exception = assertThrows(
                        ForbiddenException.class,
                        () -> ticketHistoryService.findByTicket(1L)
                );

                assertEquals(
                        "Você não tem permissão para acessar o histórico deste chamado",
                        exception.getMessage()
                );
        }

        @Test
        void shouldAllowAgentToAccessAnotherUsersTicketHistory() {
                User agent = User.builder()
                        .id(1L)
                        .role(UserRole.AGENT)
                        .build();

                User client = User.builder()
                        .id(2L)
                        .role(UserRole.CLIENT)
                        .build();

                Ticket ticket = Ticket.builder()
                        .id(1L)
                        .createdBy(client)
                        .build();

                when(ticketRepository.findById(1L))
                        .thenReturn(Optional.of(ticket));

                when(authenticatedUserProvider.getAuthenticatedUser())
                        .thenReturn(agent);

                when(ticketHistoryRepository.findByTicketIdOrderByCreatedAtAsc(1L))
                        .thenReturn(List.of());

                List<TicketHistoryResponse> response =
                        ticketHistoryService.findByTicket(1L);

                assertEquals(0, response.size());
        }
}