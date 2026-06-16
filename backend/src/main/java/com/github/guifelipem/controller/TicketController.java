package com.github.guifelipem.controller;

import com.github.guifelipem.dto.common.PageResponse;
import com.github.guifelipem.dto.ticket.CreateTicketRequest;
import com.github.guifelipem.dto.ticket.TicketResponse;
import com.github.guifelipem.dto.ticket.UpdateTicketStatusRequest;
import com.github.guifelipem.enums.TicketPriority;
import com.github.guifelipem.enums.TicketStatus;
import com.github.guifelipem.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@RequestBody @Valid CreateTicketRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.create(request));
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/me")
    public ResponseEntity<List<TicketResponse>> findMyTickets() {

        return ResponseEntity.ok(ticketService.findMyTickets());
    }

    @PreAuthorize("hasAnyRole('CLIENT', 'AGENT', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.findById(id));
    }

    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateStatus(@PathVariable Long id,
                                                       @RequestBody @Valid UpdateTicketStatusRequest request) {

        return ResponseEntity.ok(ticketService.updateStatus(id, request));
    }

    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    @PatchMapping("/{id}/assign/me")
    public ResponseEntity<TicketResponse> assignToMe(@PathVariable Long id) {

        return ResponseEntity.ok(ticketService.assignToMe(id));
    }

    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    @GetMapping
    public ResponseEntity<PageResponse<TicketResponse>> findAll(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
            ) {

        String[] sortParams = sort.split(",");

        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0])
        );

        return ResponseEntity.ok(ticketService.findAll(status, priority, search, pageable));
    }
}
