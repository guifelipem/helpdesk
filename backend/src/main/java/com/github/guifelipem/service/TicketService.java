package com.github.guifelipem.service;

import com.github.guifelipem.dto.ticket.AssignedAgentResponse;
import com.github.guifelipem.dto.ticket.CreateTicketRequest;
import com.github.guifelipem.dto.ticket.TicketResponse;
import com.github.guifelipem.dto.ticket.UpdateTicketStatusRequest;
import com.github.guifelipem.entity.Ticket;
import com.github.guifelipem.entity.TicketHistory;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.Role;
import com.github.guifelipem.enums.TicketPriority;
import com.github.guifelipem.enums.TicketStatus;
import com.github.guifelipem.exception.ForbiddenException;
import com.github.guifelipem.exception.TicketAlreadyAssignedException;
import com.github.guifelipem.exception.TicketNotFoundException;
import com.github.guifelipem.exception.UserNotFoundException;
import com.github.guifelipem.repository.TicketHistoryRepository;
import com.github.guifelipem.repository.TicketRepository;
import com.github.guifelipem.repository.UserRepository;
import com.github.guifelipem.security.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

        createHistory(savedTicket, "TICKET_CREATED", null, savedTicket.getStatus().name(), user);

        return toResponse(savedTicket);
    }

    public List<TicketResponse> findMyTickets() {

        User user = authenticatedUserProvider.getAuthenticatedUser();

        return ticketRepository.findByCreatedBy(user).stream().map(this::toResponse).toList();
    }

    private TicketResponse toResponse(Ticket ticket) {

        AssignedAgentResponse assignedTo = ticket.getAssignedTo() == null ? null
                : new AssignedAgentResponse(ticket.getAssignedTo().getId(), ticket.getAssignedTo().getName());

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                assignedTo,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }

    public TicketResponse findById(Long id) {

        Ticket ticket = ticketRepository.findById(id).orElseThrow(() ->
                    new TicketNotFoundException("Chamado não encontrado")
        );

        User user = authenticatedUserProvider.getAuthenticatedUser();

        if (user.getRole() == Role.CLIENT && !ticket.getCreatedBy().getId().equals(user.getId())) {
            throw new ForbiddenException("Você não tem permissão para visualizar este chamado");
        }

        return toResponse(ticket);
    }

    @Transactional
    public TicketResponse updateStatus(Long ticketId, UpdateTicketStatusRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId).
                orElseThrow(() -> new TicketNotFoundException("Chamado não encontrado"));

        User user = authenticatedUserProvider.getAuthenticatedUser();

        TicketStatus oldStatus = ticket.getStatus();

        ticket.setStatus(request.status());
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        createHistory(savedTicket, "STATUS_CHANGED", oldStatus.name(), request.status().name(), user);

        return toResponse(savedTicket);
    }

    @Transactional
    public TicketResponse assignToMe(Long ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Chamado não encontrado"));

        if (ticket.getAssignedTo() != null) {
            throw new TicketAlreadyAssignedException("Chamado já está atribuído a um agente");
        }

        User agent = authenticatedUserProvider.getAuthenticatedUser();

        ticket.setAssignedTo(agent);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        createHistory(ticket, "TICKET_ASSIGNED", null, agent.getName(), agent);

        return toResponse(savedTicket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> findAll(TicketStatus status, TicketPriority priority) {

        return ticketRepository.findAllWithFilters(status, priority).stream()
                .map(this::toResponse).toList();
    }

    private void createHistory(Ticket ticket, String action, String oldValue, String newValue, User performedBy) {

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
