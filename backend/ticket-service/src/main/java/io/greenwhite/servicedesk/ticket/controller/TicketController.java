package io.greenwhite.servicedesk.ticket.controller;

import io.greenwhite.servicedesk.common.dto.ApiResponse;
import io.greenwhite.servicedesk.common.enums.TicketStatus;
import io.greenwhite.servicedesk.ticket.dto.CreateTicketRequest;
import io.greenwhite.servicedesk.ticket.dto.TicketDTO;
import io.greenwhite.servicedesk.ticket.security.UserPrincipal;
import io.greenwhite.servicedesk.ticket.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Ticket controller
 */
@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<TicketDTO>> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        TicketDTO ticket = ticketService.createTicket(request, currentUser.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Ticket created successfully", ticket));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<Page<TicketDTO>>> getAllTickets(Pageable pageable) {
        Page<TicketDTO> tickets = ticketService.getAllTickets(pageable);
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<TicketDTO>> getTicket(@PathVariable UUID id) {
        TicketDTO ticket = ticketService.getTicket(id);
        return ResponseEntity.ok(ApiResponse.success(ticket));
    }

    @GetMapping("/number/{ticketNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<TicketDTO>> getTicketByNumber(@PathVariable String ticketNumber) {
        TicketDTO ticket = ticketService.getTicketByNumber(ticketNumber);
        return ResponseEntity.ok(ApiResponse.success(ticket));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<Page<TicketDTO>>> searchTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) UUID projectId,
            Pageable pageable) {
        Page<TicketDTO> tickets = ticketService.getTicketsByFilters(status, assigneeId, projectId, pageable);
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<TicketDTO>> updateTicketStatus(
            @PathVariable UUID id,
            @RequestParam TicketStatus status) {
        TicketDTO ticket = ticketService.updateTicketStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Ticket status updated", ticket));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<ApiResponse<TicketDTO>> assignTicket(
            @PathVariable UUID id,
            @RequestParam UUID assigneeId) {
        TicketDTO ticket = ticketService.assignTicket(id, assigneeId);
        return ResponseEntity.ok(ApiResponse.success("Ticket assigned successfully", ticket));
    }
}
