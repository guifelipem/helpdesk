package com.github.guifelipem.controller;

import com.github.guifelipem.dto.comment.CommentResponse;
import com.github.guifelipem.dto.comment.CreateCommentRequest;
import com.github.guifelipem.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PreAuthorize("hasAnyRole('CLIENT', 'AGENT', 'ADMIN')")
    @PostMapping
    public ResponseEntity<CommentResponse> create(@PathVariable Long ticketId,
                                                  @RequestBody @Valid CreateCommentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(commentService.create(ticketId, request));
    }

    @PreAuthorize("hasAnyRole('CLIENT', 'AGENT', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<CommentResponse>> findByTicket(@PathVariable Long ticketId) {
        return ResponseEntity.ok(commentService.findByTicket(ticketId));
    }
}
