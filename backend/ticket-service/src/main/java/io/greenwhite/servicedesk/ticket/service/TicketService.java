package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.common.enums.TicketStatus;
import io.greenwhite.servicedesk.common.exception.ResourceNotFoundException;
import io.greenwhite.servicedesk.ticket.dto.CreateTicketRequest;
import io.greenwhite.servicedesk.ticket.dto.TicketDTO;
import io.greenwhite.servicedesk.ticket.event.TicketEvent;
import io.greenwhite.servicedesk.ticket.model.Project;
import io.greenwhite.servicedesk.ticket.model.Ticket;
import io.greenwhite.servicedesk.ticket.model.User;
import io.greenwhite.servicedesk.ticket.repository.ProjectRepository;
import io.greenwhite.servicedesk.ticket.repository.TicketRepository;
import io.greenwhite.servicedesk.ticket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service for ticket operations
 */
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final WebSocketService webSocketService;

    @Transactional
    public TicketDTO createTicket(CreateTicketRequest request, UUID requesterId) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", request.getProjectId()));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", requesterId));

        Ticket ticket = Ticket.builder()
                .subject(request.getSubject())
                .description(request.getDescription())
                .status(TicketStatus.NEW)
                .priority(request.getPriority())
                .channel(request.getChannel())
                .project(project)
                .requester(requester)
                .category(request.getCategory())
                .tags(request.getTags())
                .isPublic(true)
                .build();

        // Generate ticket number
        long nextSequence = getNextTicketSequence(project.getId());
        ticket.generateTicketNumber(nextSequence);

        // Assign if provided
        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", request.getAssigneeId()));
            ticket.assignTo(assignee);
        }

        ticket = ticketRepository.save(ticket);
        TicketDTO ticketDTO = mapToDTO(ticket);

        // Broadcast ticket created event
        webSocketService.broadcastTicketEvent(TicketEvent.created(ticketDTO));

        return ticketDTO;
    }

    @Transactional(readOnly = true)
    public TicketDTO getTicket(UUID id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));
        return mapToDTO(ticket);
    }

    @Transactional(readOnly = true)
    public TicketDTO getTicketByNumber(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "number", ticketNumber));
        return mapToDTO(ticket);
    }

    @Transactional(readOnly = true)
    public Page<TicketDTO> getAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<TicketDTO> getTicketsByFilters(TicketStatus status, UUID assigneeId, UUID projectId, Pageable pageable) {
        return ticketRepository.findByFilters(status, assigneeId, projectId, pageable).map(this::mapToDTO);
    }

    @Transactional
    public TicketDTO updateTicketStatus(UUID id, TicketStatus status) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        ticket.setStatus(status);

        if (status == TicketStatus.RESOLVED) {
            ticket.resolve();
        } else if (status == TicketStatus.CLOSED) {
            ticket.close();
        } else if (status == TicketStatus.REOPENED) {
            ticket.reopen();
        }

        ticket = ticketRepository.save(ticket);
        TicketDTO ticketDTO = mapToDTO(ticket);

        // Broadcast ticket status changed event
        webSocketService.broadcastTicketEvent(TicketEvent.statusChanged(ticketDTO));

        return ticketDTO;
    }

    @Transactional
    public TicketDTO assignTicket(UUID id, UUID assigneeId) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", id));

        User assignee = userRepository.findById(assigneeId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", assigneeId));

        ticket.assignTo(assignee);
        ticket = ticketRepository.save(ticket);
        TicketDTO ticketDTO = mapToDTO(ticket);

        // Broadcast ticket assigned event
        webSocketService.broadcastTicketEvent(TicketEvent.assigned(ticketDTO));

        return ticketDTO;
    }

    private synchronized long getNextTicketSequence(UUID projectId) {
        // Simplified sequence generation - in production use a proper sequence table
        long count = ticketRepository.count();
        return count + 1;
    }

    private TicketDTO mapToDTO(Ticket ticket) {
        return TicketDTO.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .channel(ticket.getChannel())
                .projectId(ticket.getProject().getId())
                .projectName(ticket.getProject().getName())
                .requesterId(ticket.getRequester().getId())
                .requesterName(ticket.getRequester().getFullName())
                .assigneeId(ticket.getAssignee() != null ? ticket.getAssignee().getId() : null)
                .assigneeName(ticket.getAssignee() != null ? ticket.getAssignee().getFullName() : null)
                .teamId(ticket.getTeam() != null ? ticket.getTeam().getId() : null)
                .teamName(ticket.getTeam() != null ? ticket.getTeam().getName() : null)
                .category(ticket.getCategory())
                .tags(ticket.getTags())
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .firstResponseAt(ticket.getFirstResponseAt())
                .resolvedAt(ticket.getResolvedAt())
                .closedAt(ticket.getClosedAt())
                .dueDate(ticket.getDueDate())
                .isPublic(ticket.getIsPublic())
                .build();
    }
}
