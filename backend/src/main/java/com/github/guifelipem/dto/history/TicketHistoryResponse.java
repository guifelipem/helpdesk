package com.github.guifelipem.dto.history;

import com.github.guifelipem.dto.ticket.UserSummaryResponse;
import com.github.guifelipem.enums.TicketHistoryAction;

import java.time.LocalDateTime;

public record TicketHistoryResponse(

        Long id,
        TicketHistoryAction action,
        String oldValue,
        String newValue,
        UserSummaryResponse performedBy,
        LocalDateTime createdAt
) {}
