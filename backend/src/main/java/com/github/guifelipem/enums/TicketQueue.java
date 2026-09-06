package com.github.guifelipem.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Fila operacional do agente")
public enum TicketQueue {
    AVAILABLE,
    MY_TICKETS,
    WAITING_CLIENT,
    WAITING_AGENT,
    RESOLVED
}
