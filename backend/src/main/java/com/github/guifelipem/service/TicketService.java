package com.github.guifelipem.service;

import com.github.guifelipem.dto.ticket.CreateTicketRequest;
import com.github.guifelipem.dto.ticket.TicketResponse;
import com.github.guifelipem.entity.Ticket;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.TicketStatus;
import com.github.guifelipem.exception.AccessDeniedException;
import com.github.guifelipem.exception.TicketNotFoundException;
import com.github.guifelipem.exception.UserNotFoundException;
import com.github.guifelipem.repository.TicketRepository;
import com.github.guifelipem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

        return new TicketResponse(
                savedTicket.getId(),
                savedTicket.getTitle(),
                savedTicket.getDescription(),
                savedTicket.getStatus(),
                savedTicket.getPriority(),
                savedTicket.getCreatedAt()
        );
    }

    public List<TicketResponse> findMyTickets() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new UserNotFoundException("Usuário não encontrado")
        );

        return ticketRepository.findByCreatedBy(user).stream().map(this::toResponse).toList();
    }

    public TicketResponse findById(Long id) {

        Ticket ticket = ticketRepository.findById(id).orElseThrow(() ->
                    new TicketNotFoundException("Chamado não encontrado")
        );

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        if (!ticket.getCreatedBy().getEmail().equals(email)) {

            throw new AccessDeniedException("Você não tem acesso a este chamado");
        }

        return toResponse(ticket);
    }

    private TicketResponse toResponse(Ticket ticket) {

        return new TicketResponse(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCreatedAt()
        );
    }
}
