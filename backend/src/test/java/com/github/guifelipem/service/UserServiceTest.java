package com.github.guifelipem.service;

import com.github.guifelipem.dto.user.UpdateUserRoleRequest;
import com.github.guifelipem.dto.user.BlockUserRequest;
import com.github.guifelipem.dto.user.UserResponse;
import com.github.guifelipem.entity.Ticket;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.AgentBlockAction;
import com.github.guifelipem.enums.TicketPriority;
import com.github.guifelipem.enums.UserRole;
import com.github.guifelipem.exception.ForbiddenException;
import com.github.guifelipem.exception.UserNotFoundException;
import com.github.guifelipem.exception.UserHasActiveTicketsException;
import com.github.guifelipem.enums.TicketStatus;
import com.github.guifelipem.repository.TicketRepository;
import com.github.guifelipem.repository.TicketHistoryRepository;
import com.github.guifelipem.repository.UserRepository;
import com.github.guifelipem.security.AuthenticatedUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private TicketHistoryRepository ticketHistoryRepository;

    @Mock
    private AuthenticatedUserProvider authenticatedUserProvider;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldListUsersWithFiltersAndPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        User user = buildUser(1L, UserRole.AGENT);

        when(userRepository.findAllWithFilters(UserRole.AGENT, "maria", pageable))
                .thenReturn(new PageImpl<>(List.of(user), pageable, 1));

        Page<UserResponse> response = userService.findAll(UserRole.AGENT, "maria", pageable);

        assertEquals(1, response.getTotalElements());
        assertEquals(user.getId(), response.getContent().getFirst().id());
        assertEquals(user.getName(), response.getContent().getFirst().name());
        assertEquals(user.getEmail(), response.getContent().getFirst().email());
        assertEquals(user.getRole(), response.getContent().getFirst().role());
        assertEquals(user.isActive(), response.getContent().getFirst().active());
    }

    @Test
    void shouldUpdateUserRole() {
        User user = buildUser(1L, UserRole.CLIENT);
        UpdateUserRoleRequest request = new UpdateUserRoleRequest(UserRole.AGENT);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserResponse response = userService.updateRole(user.getId(), request);

        assertEquals(UserRole.AGENT, response.role());
        verify(userRepository).save(user);
    }

    @Test
    void shouldRejectRoleChangeWhenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.updateRole(1L, new UpdateUserRoleRequest(UserRole.AGENT))
        );

        assertEquals("Usuário não encontrado", exception.getMessage());
    }

    @Test
    void shouldRejectChangingAdministratorRole() {
        User administrator = buildUser(1L, UserRole.ADMIN);
        when(userRepository.findById(administrator.getId())).thenReturn(Optional.of(administrator));

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> userService.updateRole(administrator.getId(), new UpdateUserRoleRequest(UserRole.AGENT))
        );

        assertEquals("Não é permitido alterar a role de um administrador", exception.getMessage());
        verify(userRepository, never()).save(administrator);
    }

    @Test
    void shouldRejectAssigningAdministratorRole() {
        User user = buildUser(1L, UserRole.CLIENT);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> userService.updateRole(user.getId(), new UpdateUserRoleRequest(UserRole.ADMIN))
        );

        assertEquals("Não é permitido atribuir a role de administrador", exception.getMessage());
        verify(userRepository, never()).save(user);
    }

    @Test
    void shouldRejectAgentDemotionWhenActiveTicketsAreAssigned() {
        User agent = buildUser(1L, UserRole.AGENT);
        when(userRepository.findById(agent.getId())).thenReturn(Optional.of(agent));
        when(ticketRepository.existsByAssignedToAndStatusNot(agent, TicketStatus.CLOSED)).thenReturn(true);

        UserHasActiveTicketsException exception = assertThrows(
                UserHasActiveTicketsException.class,
                () -> userService.updateRole(agent.getId(), new UpdateUserRoleRequest(UserRole.CLIENT))
        );

        assertEquals(
                "Não é possível alterar a role para CLIENT enquanto o agente possui chamados ativos. "
                        + "Transfira os chamados para outro agente ou devolva-os para a fila antes de continuar.",
                exception.getMessage()
        );
        verify(userRepository, never()).save(agent);
    }

    @Test
    void shouldAllowAgentDemotionWhenThereAreNoActiveAssignedTickets() {
        User agent = buildUser(1L, UserRole.AGENT);
        when(userRepository.findById(agent.getId())).thenReturn(Optional.of(agent));
        when(ticketRepository.existsByAssignedToAndStatusNot(agent, TicketStatus.CLOSED)).thenReturn(false);
        when(userRepository.save(agent)).thenReturn(agent);

        UserResponse response = userService.updateRole(agent.getId(), new UpdateUserRoleRequest(UserRole.CLIENT));

        assertEquals(UserRole.CLIENT, response.role());
    }

    @Test
    void shouldBlockAndUnblockUser() {
        User client = buildUser(1L, UserRole.CLIENT);
        when(userRepository.findByIdForUpdate(client.getId())).thenReturn(Optional.of(client));
        when(userRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(userRepository.save(client)).thenReturn(client);

        assertEquals(false, userService.block(client.getId(), null).active());
        assertEquals(true, userService.unblock(client.getId()).active());
    }

    @Test
    void shouldReportActiveTicketCountBeforeBlockingAgent() {
        User agent = buildUser(1L, UserRole.AGENT);
        Ticket resolved = buildTicket(10L, agent, TicketStatus.RESOLVED);
        Ticket inProgress = buildTicket(11L, agent, TicketStatus.IN_PROGRESS);
        when(userRepository.findByIdForUpdate(agent.getId())).thenReturn(Optional.of(agent));
        when(ticketRepository.findActiveAssignedToForUpdate(agent, TicketStatus.CLOSED))
                .thenReturn(List.of(resolved, inProgress));

        UserHasActiveTicketsException exception = assertThrows(
                UserHasActiveTicketsException.class,
                () -> userService.block(agent.getId(), null)
        );

        assertEquals(2, exception.getActiveTicketCount());
        assertEquals(true, agent.isActive());
        verify(userRepository, never()).save(agent);
    }

    @Test
    void shouldTransferResolvedTicketAndBlockAgentInOneOperation() {
        User agent = buildUser(1L, UserRole.AGENT);
        User target = buildUser(2L, UserRole.AGENT);
        User admin = buildUser(3L, UserRole.ADMIN);
        Ticket resolved = buildTicket(10L, agent, TicketStatus.RESOLVED);
        when(userRepository.findByIdForUpdate(agent.getId())).thenReturn(Optional.of(agent));
        when(userRepository.findByIdForUpdate(target.getId())).thenReturn(Optional.of(target));
        when(ticketRepository.findActiveAssignedToForUpdate(agent, TicketStatus.CLOSED))
                .thenReturn(List.of(resolved));
        when(authenticatedUserProvider.getAuthenticatedUser()).thenReturn(admin);
        when(ticketRepository.save(resolved)).thenReturn(resolved);
        when(userRepository.save(agent)).thenReturn(agent);

        UserResponse response = userService.block(agent.getId(),
                new BlockUserRequest(AgentBlockAction.TRANSFER, target.getId()));

        assertEquals(false, response.active());
        assertEquals(target, resolved.getAssignedTo());
        assertEquals(TicketStatus.RESOLVED, resolved.getStatus());
        verify(ticketHistoryRepository).save(org.mockito.ArgumentMatchers.argThat(history ->
                history.getDetails().contains("devido ao bloqueio do agente")
        ));
    }

    private Ticket buildTicket(Long id, User assignedTo, TicketStatus status) {
        return Ticket.builder()
                .id(id)
                .title("Chamado")
                .description("Descrição")
                .status(status)
                .priority(TicketPriority.MEDIUM)
                .assignedTo(assignedTo)
                .build();
    }

    private User buildUser(Long id, UserRole role) {
        return User.builder()
                .id(id)
                .name("Maria Silva")
                .email("maria@example.com")
                .role(role)
                .build();
    }
}
