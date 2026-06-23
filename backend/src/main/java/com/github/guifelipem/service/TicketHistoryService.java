package com.github.guifelipem.service;

import com.github.guifelipem.dto.history.TicketHistoryResponse;
import com.github.guifelipem.entity.TicketHistory;
import com.github.guifelipem.repository.TicketHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketHistoryService {

    private final TicketHistoryRepository ticketHistoryRepository;

    @Transactional(readOnly = true)
    public List<TicketHistoryResponse> findByTicket(Long ticketId) {

        return ticketHistoryRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream().map(this::toResponse).toList();
    }

    private TicketHistoryResponse toResponse(TicketHistory history) {

        return new TicketHistoryResponse(
                history.getId(),
                history.getAction(),
                history.getOldValue(),
                history.getNewValue(),
                history.getPerformedBy().getId(),
                history.getPerformedBy().getName(),
                history.getCreatedAt()
        );
    }
}
