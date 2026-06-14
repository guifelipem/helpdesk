package com.github.guifelipem.dto.ticket;

import com.github.guifelipem.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateTicketStatusRequest(
        @NotNull
        TicketStatus status
) {
}
