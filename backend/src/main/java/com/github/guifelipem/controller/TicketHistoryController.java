package com.github.guifelipem.controller;

import com.github.guifelipem.dto.history.TicketHistoryResponse;
import com.github.guifelipem.service.TicketHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tickets/{ticketId}/history")
@RequiredArgsConstructor
public class TicketHistoryController {

    private final TicketHistoryService ticketHistoryService;

    @PreAuthorize("hasAnyRole('CLIENT', 'AGENT', 'ADMIN')")
    @GetMapping
    public ResponseEntity<List<TicketHistoryResponse>> findByTicket(@PathVariable Long ticketId) {

        return ResponseEntity.ok(ticketHistoryService.findByTicket(ticketId));
    }
}
