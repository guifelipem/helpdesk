package com.github.guifelipem.service;

import com.github.guifelipem.dto.history.TicketHistoryResponse;
import com.github.guifelipem.dto.ticket.UserSummaryResponse;
import com.github.guifelipem.entity.Ticket;
import com.github.guifelipem.entity.TicketHistory;
import com.github.guifelipem.entity.User;
import com.github.guifelipem.enums.UserRole;
import com.github.guifelipem.exception.ForbiddenException;
import com.github.guifelipem.exception.TicketNotFoundException;
import com.github.guifelipem.repository.TicketHistoryRepository;
import com.github.guifelipem.repository.TicketRepository;
import com.github.guifelipem.security.AuthenticatedUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketHistoryService {

    private final TicketHistoryRepository ticketHistoryRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final TicketRepository ticketRepository;

    @Transactional(readOnly = true)
    public List<TicketHistoryResponse> findByTicket(Long ticketId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Chamado não encontrado"));

        User user = authenticatedUserProvider.getAuthenticatedUser();

        if (user.getRole() == UserRole.CLIENT && !ticket.getCreatedBy().getId().equals(user.getId())) {

            throw new ForbiddenException("Você não tem permissão para acessar o histórico deste chamado");
        }

        return ticketHistoryRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream().map(this::toResponse).toList();
    }

    private TicketHistoryResponse toResponse(TicketHistory history) {

        UserSummaryResponse performedBy = new UserSummaryResponse(
                history.getPerformedBy().getId(),
                history.getPerformedBy().getName()
        );

        return new TicketHistoryResponse(
                history.getId(),
                history.getAction(),
                history.getOldValue(),
                history.getNewValue(),
                performedBy,
                history.getCreatedAt()
        );
    }
}
