package com.github.guifelipem.service;

import com.github.guifelipem.dto.user.UpdateUserRoleRequest;
import com.github.guifelipem.dto.user.BlockUserRequest;
import com.github.guifelipem.dto.user.UserResponse;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.entity.Ticket;
import com.github.guifelipem.entity.TicketHistory;
import com.github.guifelipem.enums.AgentBlockAction;
import com.github.guifelipem.enums.TicketHistoryAction;
import com.github.guifelipem.enums.UserRole;
import com.github.guifelipem.exception.ForbiddenException;
import com.github.guifelipem.exception.UserNotFoundException;
import com.github.guifelipem.exception.UserHasActiveTicketsException;
import com.github.guifelipem.exception.InvalidTicketManagementException;
import com.github.guifelipem.enums.TicketStatus;
import com.github.guifelipem.repository.TicketRepository;
import com.github.guifelipem.repository.UserRepository;
import com.github.guifelipem.repository.TicketHistoryRepository;
import com.github.guifelipem.security.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

        private final UserRepository userRepository;
        private final TicketRepository ticketRepository;
        private final TicketHistoryRepository ticketHistoryRepository;
        private final AuthenticatedUserProvider authenticatedUserProvider;

        public Page<UserResponse> findAll(UserRole role, String search, Pageable pageable) {
                Page<User> users = userRepository.findAllWithFilters(role, search, pageable);

                return users.map(this::toResponse);
        }

        public UserResponse updateRole(Long userId, UpdateUserRoleRequest request) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

                if (user.getRole() == UserRole.ADMIN) {
                        throw new ForbiddenException("Não é permitido alterar a role de um administrador");
                }

                if (request.role() == UserRole.ADMIN) {
                        throw new ForbiddenException("Não é permitido atribuir a role de administrador");
                }

                if (user.getRole() == UserRole.AGENT
                        && request.role() == UserRole.CLIENT
                        && ticketRepository.existsByAssignedToAndStatusNot(user, TicketStatus.CLOSED)) {
                        throw new UserHasActiveTicketsException(
                                "Não é possível alterar a role para CLIENT enquanto o agente possui chamados ativos. "
                                        + "Transfira os chamados para outro agente ou devolva-os para a fila antes de continuar."
                        );
                }

                user.setRole(request.role());

                User updatedUser = userRepository.save(user);

                return toResponse(updatedUser);
        }

        @Transactional
        public UserResponse block(Long userId, BlockUserRequest request) {
                User user = userRepository.findByIdForUpdate(userId)
                        .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

                validateNotAdministrator(user);

                if (user.getRole() != UserRole.AGENT) {
                        user.setActive(false);
                        return toResponse(userRepository.save(user));
                }

                List<Ticket> activeTickets = ticketRepository.findActiveAssignedToForUpdate(user, TicketStatus.CLOSED);
                if (!activeTickets.isEmpty() && (request == null || request.action() == null)) {
                        throw new UserHasActiveTicketsException(
                                "O agente possui chamados ativos que precisam ser redistribuídos antes do bloqueio.",
                                activeTickets.size()
                        );
                }

                if (!activeTickets.isEmpty()) {
                        redistributeForBlock(user, activeTickets, request);
                }

                user.setActive(false);
                return toResponse(userRepository.save(user));
        }

        public UserResponse unblock(Long userId) {
                return setActive(userId, true);
        }

        private void redistributeForBlock(User blockedAgent, List<Ticket> tickets, BlockUserRequest request) {
                User admin = authenticatedUserProvider.getAuthenticatedUser();
                User targetAgent = null;

                if (request.action() == AgentBlockAction.TRANSFER) {
                        if (request.targetAgentId() == null) {
                                throw new InvalidTicketManagementException("O agente de destino é obrigatório para a transferência");
                        }
                        targetAgent = userRepository.findByIdForUpdate(request.targetAgentId())
                                .orElseThrow(() -> new UserNotFoundException("Agente de destino não encontrado"));
                        if (targetAgent.getRole() != UserRole.AGENT || !targetAgent.isActive()) {
                                throw new InvalidTicketManagementException("O agente de destino precisa ser um AGENT ativo");
                        }
                        if (targetAgent.getId().equals(blockedAgent.getId())) {
                                throw new InvalidTicketManagementException("O agente de destino deve ser diferente do agente bloqueado");
                        }
                }

                LocalDateTime changedAt = LocalDateTime.now();
                for (Ticket ticket : tickets) {
                        User destination = targetAgent;
                        ticket.setAssignedTo(destination);
                        if (request.action() == AgentBlockAction.RETURN_TO_QUEUE) {
                                ticket.setStatus(TicketStatus.OPEN);
                        }
                        ticket.setUpdatedAt(changedAt);
                        Ticket savedTicket = ticketRepository.save(ticket);

                        ticketHistoryRepository.save(TicketHistory.builder()
                                .ticket(savedTicket)
                                .action(destination == null
                                        ? TicketHistoryAction.TICKET_RETURNED_TO_QUEUE
                                        : TicketHistoryAction.TICKET_TRANSFERRED)
                                .oldValue(blockedAgent.getName())
                                .newValue(destination == null ? "FILA" : destination.getName())
                                .details("Redistribuição realizada devido ao bloqueio do agente "
                                        + blockedAgent.getName() + " (ID " + blockedAgent.getId() + ")")
                                .performedBy(admin)
                                .createdAt(changedAt)
                                .build());
                }
        }

        private UserResponse setActive(Long userId, boolean active) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

                validateNotAdministrator(user);

                user.setActive(active);
                return toResponse(userRepository.save(user));
        }

        private void validateNotAdministrator(User user) {
                if (user.getRole() == UserRole.ADMIN) {
                        throw new ForbiddenException("Não é permitido bloquear ou desbloquear um administrador");
                }
        }

        private UserResponse toResponse(User user) {
                return new UserResponse(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole(),
                        user.isActive()
                );
        }
}
