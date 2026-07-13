package com.github.guifelipem.dto.history;

import com.github.guifelipem.dto.ticket.UserSummaryResponse;

import java.time.LocalDateTime;

public record TicketHistoryResponse(

        Long id,
        String action,
        String oldValue,
        String newValue,
        UserSummaryResponse performedBy,
        LocalDateTime createdAt
) {}
