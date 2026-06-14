package com.github.guifelipem.controller;

import com.github.guifelipem.dto.ticket.CreateTicketRequest;
import com.github.guifelipem.dto.ticket.TicketResponse;
import com.github.guifelipem.dto.ticket.UpdateTicketStatusRequest;
import com.github.guifelipem.repository.TicketRepository;
import com.github.guifelipem.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@RequestBody @Valid CreateTicketRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.create(request));
    }

    @GetMapping("/me")
    public ResponseEntity<List<TicketResponse>> findMyTickets() {

        return ResponseEntity.ok(ticketService.findMyTickets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.findById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateStatus(@PathVariable Long id,
                                                       @RequestBody @Valid UpdateTicketStatusRequest request) {

        return ResponseEntity.ok(ticketService.updateStatus(id, request));
    }
}
