package com.github.guifelipem.dto.history;

import java.time.LocalDateTime;

public record TicketHistoryResponse(

        Long id,
        String action,
        String oldValue,
        String newValue,
        Long performedById,
        String performedByName,
        LocalDateTime createdAt
) {}
