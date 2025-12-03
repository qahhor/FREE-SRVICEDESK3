package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.common.enums.NotificationChannel;
import io.greenwhite.servicedesk.common.enums.NotificationEventType;
import io.greenwhite.servicedesk.ticket.model.NotificationTemplate;
import io.greenwhite.servicedesk.ticket.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for rendering notification templates
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final NotificationTemplateRepository templateRepository;

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(\\w+)\\}\\}");

    /**
     * Get template for event type and channel
     */
    public Optional<NotificationTemplate> getTemplate(NotificationEventType eventType, NotificationChannel channel, String language) {
        // Try to find template with specific language
        Optional<NotificationTemplate> template = templateRepository.findByEventTypeAndChannelAndLanguageAndIsActiveTrue(
                eventType, channel, language);
        
        // Fallback to English if not found
        if (template.isEmpty() && !"en".equals(language)) {
            template = templateRepository.findByEventTypeAndChannelAndLanguageAndIsActiveTrue(
                    eventType, channel, "en");
        }
        
        return template;
    }

    /**
     * Render template with variables
     */
    public String renderTemplate(String template, Map<String, Object> variables) {
        if (template == null || template.isEmpty()) {
            return "";
        }
        
        StringBuffer result = new StringBuffer();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            Object value = variables.get(placeholder);
            String replacement = value != null ? String.valueOf(value) : "";
            // Escape special regex characters in replacement
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    /**
     * Render notification title
     */
    public String renderTitle(NotificationEventType eventType, NotificationChannel channel, 
                              Map<String, Object> variables, String language) {
        Optional<NotificationTemplate> template = getTemplate(eventType, channel, language);
        if (template.isPresent()) {
            String subject = template.get().getSubject();
            return subject != null ? renderTemplate(subject, variables) : getDefaultTitle(eventType);
        }
        return getDefaultTitle(eventType);
    }

    /**
     * Render notification message body
     */
    public String renderMessage(NotificationEventType eventType, NotificationChannel channel, 
                                Map<String, Object> variables, String language) {
        Optional<NotificationTemplate> template = getTemplate(eventType, channel, language);
        if (template.isPresent()) {
            return renderTemplate(template.get().getBodyTemplate(), variables);
        }
        return getDefaultMessage(eventType, variables);
    }

    /**
     * Get default title for event type
     */
    private String getDefaultTitle(NotificationEventType eventType) {
        return switch (eventType) {
            case TICKET_CREATED -> "New Ticket Created";
            case TICKET_ASSIGNED -> "Ticket Assigned";
            case TICKET_UNASSIGNED -> "Ticket Unassigned";
            case TICKET_COMMENTED -> "New Comment";
            case TICKET_STATUS_CHANGED -> "Ticket Status Changed";
            case TICKET_PRIORITY_CHANGED -> "Ticket Priority Changed";
            case TICKET_RESOLVED -> "Ticket Resolved";
            case TICKET_CLOSED -> "Ticket Closed";
            case TICKET_REOPENED -> "Ticket Reopened";
            case SLA_WARNING -> "SLA Warning";
            case SLA_BREACHED -> "SLA Breached";
            case MENTION_IN_COMMENT -> "You were mentioned";
            case USER_REGISTERED -> "Welcome";
            case PASSWORD_RESET -> "Password Reset";
        };
    }

    /**
     * Get default message for event type
     */
    private String getDefaultMessage(NotificationEventType eventType, Map<String, Object> variables) {
        String ticketNumber = variables.getOrDefault("ticketNumber", "").toString();
        
        return switch (eventType) {
            case TICKET_CREATED -> "A new ticket " + ticketNumber + " has been created";
            case TICKET_ASSIGNED -> "Ticket " + ticketNumber + " has been assigned to you";
            case TICKET_UNASSIGNED -> "You have been unassigned from ticket " + ticketNumber;
            case TICKET_COMMENTED -> "A new comment was added to ticket " + ticketNumber;
            case TICKET_STATUS_CHANGED -> "The status of ticket " + ticketNumber + " has been changed";
            case TICKET_PRIORITY_CHANGED -> "The priority of ticket " + ticketNumber + " has been changed";
            case TICKET_RESOLVED -> "Ticket " + ticketNumber + " has been resolved";
            case TICKET_CLOSED -> "Ticket " + ticketNumber + " has been closed";
            case TICKET_REOPENED -> "Ticket " + ticketNumber + " has been reopened";
            case SLA_WARNING -> "Ticket " + ticketNumber + " is approaching SLA breach";
            case SLA_BREACHED -> "Ticket " + ticketNumber + " has breached SLA";
            case MENTION_IN_COMMENT -> "You were mentioned in a comment on ticket " + ticketNumber;
            case USER_REGISTERED -> "Welcome to Service Desk!";
            case PASSWORD_RESET -> "Your password reset request";
        };
    }
}
