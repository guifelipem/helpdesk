package com.github.guifelipem.service;

import com.github.guifelipem.dto.ticket.AssignedAgentResponse;
import com.github.guifelipem.dto.ticket.CreateTicketRequest;
import com.github.guifelipem.dto.ticket.TicketResponse;
import com.github.guifelipem.dto.ticket.UpdateTicketStatusRequest;
import com.github.guifelipem.entity.Ticket;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.TicketPriority;
import com.github.guifelipem.enums.TicketStatus;
import com.github.guifelipem.exception.TicketAlreadyAssignedException;
import com.github.guifelipem.exception.TicketNotFoundException;
import com.github.guifelipem.exception.UserNotFoundException;
import com.github.guifelipem.repository.TicketRepository;
import com.github.guifelipem.repository.UserRepository;
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
    private final UserRepository userRepository;

    public TicketResponse create(CreateTicketRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new UserNotFoundException("Usuário não encontrado")
        );

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

        return toResponse(savedTicket);
    }

    public List<TicketResponse> findMyTickets() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new UserNotFoundException("Usuário não encontrado")
        );

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

        return toResponse(ticket);
    }

    @Transactional
    public TicketResponse updateStatus(Long ticketId, UpdateTicketStatusRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId).
                orElseThrow(() -> new TicketNotFoundException("Chamado não encontrado"));

        ticket.setStatus(request.status());
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        return toResponse(savedTicket);
    }

    @Transactional
    public TicketResponse assignToMe(Long ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Chamado não encontrado"));

        if (ticket.getAssignedTo() != null) {
            throw new TicketAlreadyAssignedException("Chamado já está atribuído a um agente");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User agent = userRepository.findByEmail(email).orElseThrow(() ->
                new UserNotFoundException("Usuário não encontrado")
        );

        ticket.setAssignedTo(agent);
        ticket.setStatus(TicketStatus.IN_PROGRESS);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        return toResponse(savedTicket);
    }

    public List<TicketResponse> findAll(TicketStatus status, TicketPriority priority) {

        return ticketRepository.findAll().stream()
                .filter(ticket -> status == null || ticket.getStatus() == status)
                .filter(ticket -> priority == null || ticket.getPriority() == priority)
                .map(this::toResponse).toList();
    }

}
