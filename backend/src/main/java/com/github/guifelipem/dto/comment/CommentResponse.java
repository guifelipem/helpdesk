package com.github.guifelipem.dto.comment;

import com.github.guifelipem.dto.ticket.UserSummaryResponse;

import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

public record CommentResponse(

        Long id,
        String message,
        @Schema(description = "Indica se o comentário é restrito à equipe") Boolean isInternal,
        UserSummaryResponse author,
        LocalDateTime createdAt
) {}
