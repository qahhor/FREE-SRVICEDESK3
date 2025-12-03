package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.common.enums.NotificationCategory;
import io.greenwhite.servicedesk.common.enums.NotificationEventType;
import io.greenwhite.servicedesk.ticket.dto.NotificationResponse;
import io.greenwhite.servicedesk.ticket.dto.TicketDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Main notification service that orchestrates all notification channels
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final InAppNotificationService inAppNotificationService;

    /**
     * Notify about ticket creation
     */
    public void notifyTicketCreated(TicketDTO ticket, UUID requesterId, UUID assigneeId) {
        Map<String, Object> data = buildTicketData(ticket);
        
        // Notify requester
        if (requesterId != null) {
            sendInAppNotification(requesterId, NotificationEventType.TICKET_CREATED, 
                    NotificationCategory.TICKET, data, "TICKET", ticket.getId());
        }
        
        // Notify assignee if assigned
        if (assigneeId != null && !assigneeId.equals(requesterId)) {
            sendInAppNotification(assigneeId, NotificationEventType.TICKET_CREATED, 
                    NotificationCategory.TICKET, data, "TICKET", ticket.getId());
        }
        
        log.info("Ticket created notifications sent for ticket {}", ticket.getTicketNumber());
    }

    /**
     * Notify about ticket assignment
     */
    public void notifyTicketAssigned(TicketDTO ticket, UUID assigneeId, UUID previousAssigneeId) {
        Map<String, Object> data = buildTicketData(ticket);
        
        // Notify new assignee
        if (assigneeId != null) {
            sendInAppNotification(assigneeId, NotificationEventType.TICKET_ASSIGNED, 
                    NotificationCategory.TICKET, data, "TICKET", ticket.getId());
        }
        
        // Notify previous assignee about unassignment
        if (previousAssigneeId != null && !previousAssigneeId.equals(assigneeId)) {
            sendInAppNotification(previousAssigneeId, NotificationEventType.TICKET_UNASSIGNED, 
                    NotificationCategory.TICKET, data, "TICKET", ticket.getId());
        }
        
        log.info("Ticket assignment notifications sent for ticket {}", ticket.getTicketNumber());
    }

    /**
     * Notify about new comment on ticket
     */
    public void notifyTicketCommented(TicketDTO ticket, UUID commentAuthorId, String commentAuthorName, 
                                      UUID commentId, boolean isInternal) {
        Map<String, Object> data = buildTicketData(ticket);
        data.put("commentAuthor", commentAuthorName);
        data.put("commentId", commentId.toString());
        data.put("isInternal", isInternal);
        
        // Notify ticket requester (if not the comment author and comment is public)
        if (!isInternal && ticket.getRequesterId() != null && !ticket.getRequesterId().equals(commentAuthorId)) {
            sendInAppNotification(ticket.getRequesterId(), NotificationEventType.TICKET_COMMENTED, 
                    NotificationCategory.COMMENT, data, "COMMENT", commentId);
        }
        
        // Notify assignee (if exists and not the comment author)
        if (ticket.getAssigneeId() != null && !ticket.getAssigneeId().equals(commentAuthorId)) {
            sendInAppNotification(ticket.getAssigneeId(), NotificationEventType.TICKET_COMMENTED, 
                    NotificationCategory.COMMENT, data, "COMMENT", commentId);
        }
        
        log.info("Comment notification sent for ticket {}", ticket.getTicketNumber());
    }

    /**
     * Notify about ticket status change
     */
    public void notifyTicketStatusChanged(TicketDTO ticket, String previousStatus, UUID changedByUserId) {
        Map<String, Object> data = buildTicketData(ticket);
        data.put("previousStatus", previousStatus);
        data.put("newStatus", ticket.getStatus().name());
        
        NotificationEventType eventType = switch (ticket.getStatus()) {
            case RESOLVED -> NotificationEventType.TICKET_RESOLVED;
            case CLOSED -> NotificationEventType.TICKET_CLOSED;
            case REOPENED -> NotificationEventType.TICKET_REOPENED;
            default -> NotificationEventType.TICKET_STATUS_CHANGED;
        };
        
        // Notify requester (if not the one who changed status)
        if (ticket.getRequesterId() != null && !ticket.getRequesterId().equals(changedByUserId)) {
            sendInAppNotification(ticket.getRequesterId(), eventType, 
                    NotificationCategory.TICKET, data, "TICKET", ticket.getId());
        }
        
        // Notify assignee (if exists and not the one who changed status)
        if (ticket.getAssigneeId() != null && !ticket.getAssigneeId().equals(changedByUserId) 
                && !ticket.getAssigneeId().equals(ticket.getRequesterId())) {
            sendInAppNotification(ticket.getAssigneeId(), eventType, 
                    NotificationCategory.TICKET, data, "TICKET", ticket.getId());
        }
        
        log.info("Ticket status change notification sent for ticket {}", ticket.getTicketNumber());
    }

    /**
     * Notify about SLA warning
     */
    public void notifySlaWarning(TicketDTO ticket, String timeRemaining) {
        Map<String, Object> data = buildTicketData(ticket);
        data.put("timeRemaining", timeRemaining);
        
        // Notify assignee
        if (ticket.getAssigneeId() != null) {
            sendInAppNotification(ticket.getAssigneeId(), NotificationEventType.SLA_WARNING, 
                    NotificationCategory.SLA, data, "TICKET", ticket.getId());
        }
        
        log.info("SLA warning notification sent for ticket {}", ticket.getTicketNumber());
    }

    /**
     * Notify about SLA breach
     */
    public void notifySlaBreached(TicketDTO ticket) {
        Map<String, Object> data = buildTicketData(ticket);
        
        // Notify assignee
        if (ticket.getAssigneeId() != null) {
            sendInAppNotification(ticket.getAssigneeId(), NotificationEventType.SLA_BREACHED, 
                    NotificationCategory.SLA, data, "TICKET", ticket.getId());
        }
        
        log.info("SLA breach notification sent for ticket {}", ticket.getTicketNumber());
    }

    /**
     * Send a generic in-app notification
     */
    public NotificationResponse sendInAppNotification(UUID userId, NotificationEventType eventType,
                                                      NotificationCategory category, Map<String, Object> data,
                                                      String referenceType, UUID referenceId) {
        try {
            return inAppNotificationService.sendNotification(userId, eventType, category, data, referenceType, referenceId);
        } catch (Exception e) {
            log.error("Failed to send in-app notification to user {}: {}", userId, e.getMessage());
            return null;
        }
    }

    /**
     * Build common ticket data for templates
     */
    private Map<String, Object> buildTicketData(TicketDTO ticket) {
        Map<String, Object> data = new HashMap<>();
        data.put("ticketId", ticket.getId().toString());
        data.put("ticketNumber", ticket.getTicketNumber());
        data.put("ticketSubject", ticket.getSubject());
        data.put("ticketPriority", ticket.getPriority().name());
        data.put("ticketStatus", ticket.getStatus().name());
        data.put("projectName", ticket.getProjectName());
        if (ticket.getAssigneeName() != null) {
            data.put("assigneeName", ticket.getAssigneeName());
        }
        if (ticket.getRequesterName() != null) {
            data.put("requesterName", ticket.getRequesterName());
        }
        return data;
    }
}
