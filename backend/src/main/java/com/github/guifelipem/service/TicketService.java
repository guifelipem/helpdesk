package com.github.guifelipem.service;

import com.github.guifelipem.dto.common.PageResponse;
import com.github.guifelipem.dto.ticket.UserSummaryResponse;
import com.github.guifelipem.dto.ticket.CreateTicketRequest;
import com.github.guifelipem.dto.ticket.TicketResponse;
import com.github.guifelipem.dto.ticket.UpdateTicketStatusRequest;
import com.github.guifelipem.entity.Ticket;
import com.github.guifelipem.entity.TicketHistory;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.TicketHistoryAction;
import com.github.guifelipem.enums.UserRole;
import com.github.guifelipem.enums.TicketPriority;
import com.github.guifelipem.enums.TicketStatus;
import com.github.guifelipem.exception.ForbiddenException;
import com.github.guifelipem.exception.InvalidTicketStatusTransitionException;
import com.github.guifelipem.exception.TicketAlreadyAssignedException;
import com.github.guifelipem.exception.TicketNotFoundException;
import com.github.guifelipem.repository.TicketHistoryRepository;
import com.github.guifelipem.repository.TicketRepository;
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
public class TicketService {

    private final TicketRepository ticketRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final TicketHistoryRepository ticketHistoryRepository;

    public TicketResponse create(CreateTicketRequest request) {

        User user = authenticatedUserProvider.getAuthenticatedUser();

        Ticket ticket = Ticket.builder()
                .title(request.title())
                .description(request.description())
                .priority(request.priority())
                .status(TicketStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(user)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);

        createHistory(savedTicket, TicketHistoryAction.TICKET_CREATED, null, savedTicket.getStatus().name(), user);

        return toResponse(savedTicket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> findMyTickets() {

        User user = authenticatedUserProvider.getAuthenticatedUser();

        return ticketRepository.findByCreatedBy(user).stream().map(this::toResponse).toList();
    }

    private TicketResponse toResponse(Ticket ticket) {

        UserSummaryResponse createdBy = new UserSummaryResponse(
                ticket.getCreatedBy().getId(),
                ticket.getCreatedBy().getName(),
                ticket.getCreatedBy().getRole()
        );

        UserSummaryResponse assignedTo = ticket.getAssignedTo() == null ? null
                : new UserSummaryResponse(
                        ticket.getAssignedTo().getId(),
                        ticket.getAssignedTo().getName(),
                        ticket.getAssignedTo().getRole()
                );

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                createdBy,
                assignedTo,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    private Ticket findTicketById(Long ticketId) {
        return ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new TicketNotFoundException("Chamado não encontrado")
                );
    }

    @Transactional(readOnly = true)
    public TicketResponse findById(Long id) {

        Ticket ticket = findTicketById(id);

        User user = authenticatedUserProvider.getAuthenticatedUser();

        if (user.getRole() == UserRole.CLIENT && !ticket.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("Você não tem permissão para visualizar este chamado");
        }

        if (user.getRole() != UserRole.CLIENT && isAssignedToAnotherUser(ticket, user)) {
            throw new ForbiddenException("Somente o responsável pode visualizar este chamado");
        }

        return toResponse(ticket);
    }

    @Transactional
    public TicketResponse updateStatus(Long ticketId, UpdateTicketStatusRequest request) {

        Ticket ticket = findTicketById(ticketId);

        User user = authenticatedUserProvider.getAuthenticatedUser();

        if (ticket.getAssignedTo() == null || !ticket.getAssignedTo().getId().equals(user.getId())) {
            throw new ForbiddenException("Somente o responsável pode alterar o status deste chamado");
        }

        TicketStatus currentStatus = ticket.getStatus();
        TicketStatus newStatus = request.status();

        if (newStatus == TicketStatus.CLOSED) {
            throw new ForbiddenException("O fechamento do chamado deve ser confirmado pelo cliente");
        }

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new InvalidTicketStatusTransitionException(
                    "Transição de status inválida: " + currentStatus + " -> " + newStatus
            );
        }

        ticket.setStatus(newStatus);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        createHistory(savedTicket, TicketHistoryAction.STATUS_CHANGED, currentStatus.name(), request.status().name(), user);

        return toResponse(savedTicket);
    }

    @Transactional
    public TicketResponse closeTicket(Long ticketId) {

        Ticket ticket = findTicketById(ticketId);

        User user = authenticatedUserProvider.getAuthenticatedUser();

        if (!ticket.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("Você não tem permissão para fechar este chamado");
        }

        if (ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new InvalidTicketStatusTransitionException("Apenas chamados resolvidos podem ser fechados");
        }

        TicketStatus currentStatus = ticket.getStatus();

        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        createHistory(savedTicket, TicketHistoryAction.STATUS_CHANGED, currentStatus.name(), TicketStatus.CLOSED.name(), user);

        return toResponse(savedTicket);
    }

    @Transactional
    public TicketResponse assignToMe(Long ticketId) {
        User agent = authenticatedUserProvider.getAuthenticatedUser();
        LocalDateTime assignmentTime = LocalDateTime.now();

        int affectedRows = ticketRepository.assignIfAvailable(ticketId, agent, TicketStatus.OPEN, TicketStatus.IN_PROGRESS, assignmentTime);

        if (affectedRows == 0 ) {
            Ticket ticket = findTicketById(ticketId);

            if (ticket.getAssignedTo() != null ) {
                throw new TicketAlreadyAssignedException("Chamado já está atribuído a um agente");
            }

            throw new InvalidTicketStatusTransitionException("Apenas chamados em aberto podem ser atribuídos");
        }

        Ticket ticketUpdated = findTicketById(ticketId);

        createHistory(ticketUpdated, TicketHistoryAction.STATUS_CHANGED, TicketStatus.OPEN.name(), TicketStatus.IN_PROGRESS.name(), agent);
        createHistory(ticketUpdated, TicketHistoryAction.TICKET_ASSIGNED, null, agent.getName(), agent);

        return toResponse(ticketUpdated);
    }

    @Transactional(readOnly = true)
    public PageResponse<TicketResponse> findAll(
            TicketStatus status,
            TicketPriority priority,
            String search,
            Pageable pageable
    ) {

        User currentUser = authenticatedUserProvider.getAuthenticatedUser();

        String normalizedSearch = search == null || search.isBlank() ? null : search.trim();

        Page<Ticket> tickets;

        if (currentUser.getRole() == UserRole.AGENT) {
            tickets = ticketRepository.findAllVisibleToAgentWithFilters(
                    currentUser.getId(),
                    status,
                    priority,
                    normalizedSearch,
                    pageable
            );
        } else {
            tickets = ticketRepository.findAllWithFilters(
                    status,
                    priority,
                    normalizedSearch,
                    pageable
            );
        }

        return new PageResponse<>(
                tickets.getContent().stream().map(this::toResponse).toList(),
                tickets.getNumber(),
                tickets.getSize(),
                tickets.getTotalElements(),
                tickets.getTotalPages(),
                tickets.isLast()
        );
    }

    private boolean isAssignedToAnotherUser(Ticket ticket, User user) {
        return ticket.getAssignedTo() != null
                && !ticket.getAssignedTo().getId().equals(user.getId());
    }

    private void createHistory(Ticket ticket, TicketHistoryAction action, String oldValue, String newValue, User performedBy) {

        TicketHistory history = TicketHistory.builder()
                .ticket(ticket)
                .action(action)
                .oldValue(oldValue)
                .newValue(newValue)
                .performedBy(performedBy)
                .createdAt(LocalDateTime.now())
                .build();

        ticketHistoryRepository.save(history);
    }
}
