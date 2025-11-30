package io.greenwhite.servicedesk.channel.service;

import io.greenwhite.servicedesk.channel.config.EmailProperties;
import io.greenwhite.servicedesk.channel.model.EmailMessage;
import io.greenwhite.servicedesk.channel.repository.EmailMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * Service for processing incoming emails and creating tickets
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailProcessorService {

    private final EmailMessageRepository emailMessageRepository;
    private final EmailProperties emailProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Process incoming email and create ticket or add comment
     */
    @Transactional
    public void processEmail(EmailMessage emailMessage) {
        log.info("Processing email: {}", emailMessage.getSubject());

        try {
            // Check if this is a reply to an existing ticket
            if (isReplyToTicket(emailMessage)) {
                addCommentToTicket(emailMessage);
            } else {
                createTicketFromEmail(emailMessage);
            }

            // Update status
            emailMessage.setStatus(EmailMessage.EmailStatus.PROCESSED);
            emailMessageRepository.save(emailMessage);

            log.info("Email processed successfully: {}", emailMessage.getId());

        } catch (Exception e) {
            log.error("Error processing email: {}", e.getMessage(), e);
            emailMessage.setStatus(EmailMessage.EmailStatus.FAILED);
            emailMessage.setErrorMessage(e.getMessage());
            emailMessageRepository.save(emailMessage);
        }
    }

    /**
     * Check if email is a reply to an existing ticket
     */
    private boolean isReplyToTicket(EmailMessage emailMessage) {
        // Check if subject contains ticket number pattern (e.g., [DESK-123])
        String subject = emailMessage.getSubject();
        if (subject != null && subject.matches(".*\\[([A-Z]+-\\d+)\\].*")) {
            return true;
        }

        // Check if In-Reply-To references a previous email linked to a ticket
        if (emailMessage.getInReplyTo() != null) {
            return emailMessageRepository.findByMessageId(emailMessage.getInReplyTo())
                .map(parentEmail -> parentEmail.getTicketId() != null)
                .orElse(false);
        }

        return false;
    }

    /**
     * Create a new ticket from email
     */
    private void createTicketFromEmail(EmailMessage emailMessage) {
        log.info("Creating ticket from email: {}", emailMessage.getSubject());

        if (!emailProperties.getProcessing().isAutoCreateTicket()) {
            log.info("Auto-create ticket is disabled");
            return;
        }

        // TODO: Call Ticket Service REST API to create ticket
        // For now, just log
        String subject = truncate(emailMessage.getSubject(),
            emailProperties.getProcessing().getMaxSubjectLength());
        String description = truncate(emailMessage.getBodyText() != null
            ? emailMessage.getBodyText()
            : emailMessage.getBodyHtml(),
            emailProperties.getProcessing().getMaxBodyLength());

        log.info("Would create ticket: subject={}, from={}", subject, emailMessage.getFromAddress());

        // In production:
        // - Call POST /api/v1/tickets with ticket creation request
        // - Store returned ticket ID in emailMessage.ticketId
        // - Handle attachments if extractAttachments is enabled
    }

    /**
     * Add comment to existing ticket
     */
    private void addCommentToTicket(EmailMessage emailMessage) {
        log.info("Adding comment to ticket from email: {}", emailMessage.getSubject());

        // Extract ticket number from subject
        String ticketNumber = extractTicketNumber(emailMessage.getSubject());
        if (ticketNumber != null) {
            emailMessage.setTicketId(ticketNumber);
            emailMessageRepository.save(emailMessage);
        }

        // TODO: Call Ticket Service REST API to add comment
        log.info("Would add comment to ticket: {}", ticketNumber);

        // In production:
        // - Extract ticket ID from subject or find by In-Reply-To
        // - Call POST /api/v1/tickets/{ticketId}/comments
        // - Include email body as comment content
    }

    /**
     * Extract ticket number from subject line
     */
    private String extractTicketNumber(String subject) {
        if (subject == null) return null;

        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\[([A-Z]+-\\d+)\\]");
        java.util.regex.Matcher matcher = pattern.matcher(subject);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * Truncate string to max length
     */
    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}
