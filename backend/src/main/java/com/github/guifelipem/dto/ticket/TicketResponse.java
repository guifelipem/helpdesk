package com.github.guifelipem.dto.ticket;

import com.github.guifelipem.enums.TicketPriority;
import com.github.guifelipem.enums.TicketStatus;

import java.time.LocalDateTime;

public record TicketResponse(

        Long id,
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        AssignedAgentResponse assignedTo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
