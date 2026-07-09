package com.github.guifelipem.dto.comment;

import com.github.guifelipem.dto.ticket.UserSummaryResponse;

import java.time.LocalDateTime;

public record CommentResponse(

        Long id,
        String message,
        Boolean isInternal,
        UserSummaryResponse author,
        LocalDateTime createdAt
) {}
