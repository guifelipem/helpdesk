package com.github.guifelipem.dto.user;

import com.github.guifelipem.enums.AgentBlockAction;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estratégia para redistribuir chamados ativos antes de bloquear um agente")
public record BlockUserRequest(
        @Schema(description = "TRANSFER ou RETURN_TO_QUEUE", example = "TRANSFER")
        AgentBlockAction action,
        @Schema(description = "Obrigatório quando action for TRANSFER", example = "8")
        Long targetAgentId
) {}
