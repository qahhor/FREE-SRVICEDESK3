package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.common.enums.ChannelType;
import io.greenwhite.servicedesk.common.enums.TicketPriority;
import io.greenwhite.servicedesk.common.enums.TicketStatus;
import io.greenwhite.servicedesk.common.exception.ResourceNotFoundException;
import io.greenwhite.servicedesk.ticket.dto.*;
import io.greenwhite.servicedesk.ticket.model.*;
import io.greenwhite.servicedesk.ticket.model.WidgetMessage.MessageType;
import io.greenwhite.servicedesk.ticket.model.WidgetMessage.SenderType;
import io.greenwhite.servicedesk.ticket.model.WidgetSession.WidgetSessionStatus;
import io.greenwhite.servicedesk.ticket.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for managing widget sessions and messages
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WidgetSessionService {

    private final WidgetSessionRepository sessionRepository;
    private final WidgetMessageRepository messageRepository;
    private final ProjectRepository projectRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final AttachmentRepository attachmentRepository;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Start a new widget session
     */
    @Transactional
    public WidgetSessionResponse startSession(WidgetSessionRequest request, String ipAddress, String userAgent) {
        // Find project by key
        Project project = projectRepository.findByKey(request.getProjectKey())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with key: " + request.getProjectKey()));

        // Generate unique session token
        String sessionToken = generateSessionToken();

        // Create session
        WidgetSession session = WidgetSession.builder()
                .sessionToken(sessionToken)
                .visitorName(request.getVisitorName())
                .visitorEmail(request.getVisitorEmail())
                .visitorMetadata(request.getVisitorMetadata())
                .project(project)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .status(WidgetSessionStatus.ACTIVE)
                .build();

        session = sessionRepository.save(session);
        log.info("Created widget session: {} for project: {}", session.getId(), project.getKey());

        return WidgetSessionResponse.fromEntity(session);
    }

    /**
     * Get session by ID
     */
    @Transactional(readOnly = true)
    public WidgetSessionResponse getSession(UUID sessionId) {
        WidgetSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));
        return WidgetSessionResponse.fromEntity(session);
    }

    /**
     * Get session by token
     */
    @Transactional(readOnly = true)
    public WidgetSessionResponse getSessionByToken(String sessionToken) {
        WidgetSession session = sessionRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found"));
        return WidgetSessionResponse.fromEntity(session);
    }

    /**
     * Validate session token and return session
     */
    @Transactional(readOnly = true)
    public WidgetSession validateSessionToken(String sessionToken) {
        return sessionRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid session token"));
    }

    /**
     * Send a message from visitor
     */
    @Transactional
    public WidgetMessageResponse sendVisitorMessage(UUID sessionId, WidgetMessageRequest request, String sessionToken) {
        WidgetSession session = sessionRepository.findByIdAndStatus(sessionId, WidgetSessionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active session not found: " + sessionId));

        // Validate token matches
        if (!session.getSessionToken().equals(sessionToken)) {
            throw new ResourceNotFoundException("Invalid session token");
        }

        // Create message
        WidgetMessage message = WidgetMessage.builder()
                .session(session)
                .senderType(SenderType.VISITOR)
                .content(request.getContent())
                .messageType(parseMessageType(request.getMessageType()))
                .build();

        // Attach file if provided
        if (request.getAttachmentId() != null) {
            Attachment attachment = attachmentRepository.findById(request.getAttachmentId())
                    .orElse(null);
            message.setAttachment(attachment);
        }

        message = messageRepository.save(message);
        log.debug("Visitor message sent in session {}: {}", sessionId, message.getId());

        // Create or update ticket for the session
        ensureTicketExists(session, request.getContent());

        return WidgetMessageResponse.fromEntity(message, session.getVisitorName());
    }

    /**
     * Send a message from agent
     */
    @Transactional
    public WidgetMessageResponse sendAgentMessage(UUID sessionId, WidgetMessageRequest request, UUID agentId) {
        WidgetSession session = sessionRepository.findByIdAndStatus(sessionId, WidgetSessionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active session not found: " + sessionId));

        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found: " + agentId));

        // Create message
        WidgetMessage message = WidgetMessage.builder()
                .session(session)
                .senderType(SenderType.AGENT)
                .senderId(agentId)
                .content(request.getContent())
                .messageType(parseMessageType(request.getMessageType()))
                .build();

        // Attach file if provided
        if (request.getAttachmentId() != null) {
            Attachment attachment = attachmentRepository.findById(request.getAttachmentId())
                    .orElse(null);
            message.setAttachment(attachment);
        }

        message = messageRepository.save(message);
        log.debug("Agent message sent in session {}: {}", sessionId, message.getId());

        String senderName = agent.getFirstName() + " " + agent.getLastName();
        return WidgetMessageResponse.fromEntity(message, senderName);
    }

    /**
     * Get all messages for a session
     */
    @Transactional(readOnly = true)
    public List<WidgetMessageResponse> getMessages(UUID sessionId) {
        // Verify session exists
        WidgetSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        return messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(msg -> {
                    String senderName = null;
                    if (msg.getSenderType() == SenderType.VISITOR) {
                        senderName = session.getVisitorName();
                    } else if (msg.getSenderType() == SenderType.AGENT && msg.getSenderId() != null) {
                        User agent = userRepository.findById(msg.getSenderId()).orElse(null);
                        if (agent != null) {
                            senderName = agent.getFirstName() + " " + agent.getLastName();
                        }
                    }
                    return WidgetMessageResponse.fromEntity(msg, senderName);
                })
                .collect(Collectors.toList());
    }

    /**
     * Close a session
     */
    @Transactional
    public void closeSession(UUID sessionId, String sessionToken) {
        WidgetSession session = sessionRepository.findByIdAndStatus(sessionId, WidgetSessionStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active session not found: " + sessionId));

        // Validate token matches
        if (!session.getSessionToken().equals(sessionToken)) {
            throw new ResourceNotFoundException("Invalid session token");
        }

        session.close();
        sessionRepository.save(session);
        log.info("Closed widget session: {}", sessionId);

        // Add system message
        WidgetMessage systemMessage = WidgetMessage.builder()
                .session(session)
                .senderType(SenderType.SYSTEM)
                .content("Session closed by visitor")
                .messageType(MessageType.TEXT)
                .build();
        messageRepository.save(systemMessage);
    }

    /**
     * Get widget configuration for a project
     */
    @Transactional(readOnly = true)
    public WidgetConfigResponse getConfig(String projectKey) {
        Project project = projectRepository.findByKey(projectKey)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with key: " + projectKey));

        return WidgetConfigResponse.builder()
                .projectKey(project.getKey())
                .projectName(project.getName())
                .online(true) // TODO: Check if agents are online
                .greeting("Hi! How can we help you?")
                .offlineMessage("We are currently offline. Leave a message!")
                .primaryColor("#1976d2")
                .maxFileSize(10 * 1024 * 1024) // 10MB
                .allowedFileTypes(new String[]{"image/*", "application/pdf", ".doc", ".docx", ".txt"})
                .build();
    }

    /**
     * Create or update ticket for the session
     */
    private void ensureTicketExists(WidgetSession session, String firstMessage) {
        if (session.getTicket() != null) {
            return; // Ticket already exists
        }

        // Create a new ticket
        Ticket ticket = Ticket.builder()
                .subject("Widget conversation from " + (session.getVisitorName() != null ? session.getVisitorName() : "Visitor"))
                .description(firstMessage)
                .status(TicketStatus.NEW)
                .priority(TicketPriority.MEDIUM)
                .channel(ChannelType.WEB_WIDGET)
                .project(session.getProject())
                .isPublic(true)
                .build();

        // Set requester if visitor email matches existing user
        if (session.getVisitorEmail() != null) {
            userRepository.findByEmail(session.getVisitorEmail())
                    .ifPresent(ticket::setRequester);
        }

        // If no requester found, use first user as fallback
        if (ticket.getRequester() == null) {
            userRepository.findFirstByOrderByCreatedAtAsc()
                    .ifPresent(ticket::setRequester);
        }

        // Generate ticket number
        long count = ticketRepository.count() + 1;
        ticket.generateTicketNumber(count);

        ticket = ticketRepository.save(ticket);
        
        // Link ticket to session
        session.setTicket(ticket);
        sessionRepository.save(session);
        
        log.info("Created ticket {} for widget session {}", ticket.getTicketNumber(), session.getId());
    }

    /**
     * Generate secure session token
     */
    private String generateSessionToken() {
        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Parse message type string to enum
     */
    private MessageType parseMessageType(String type) {
        if (type == null) {
            return MessageType.TEXT;
        }
        try {
            return MessageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MessageType.TEXT;
        }
    }
}
