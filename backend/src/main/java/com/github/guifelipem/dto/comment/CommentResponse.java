package com.github.guifelipem.dto.comment;

import java.time.LocalDateTime;

public record CommentResponse(

        Long id,
        String message,
        Boolean isInternal,
        Long authorId,
        String authorName,
        LocalDateTime createdAt
) {}
