package io.greenwhite.servicedesk.channel.service;

import io.greenwhite.servicedesk.channel.config.EmailProperties;
import io.greenwhite.servicedesk.channel.model.EmailMessage;
import io.greenwhite.servicedesk.channel.repository.EmailMessageRepository;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.FlagTerm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Service for polling emails from IMAP server
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailPollerService {

    private final EmailProperties emailProperties;
    private final EmailMessageRepository emailMessageRepository;
    private final EmailProcessorService emailProcessorService;

    /**
     * Poll emails every configured interval
     */
    @Scheduled(fixedDelayString = "${channel.email.imap.poll-interval-seconds:60}000")
    public void pollEmails() {
        if (!emailProperties.isEnabled()) {
            log.debug("Email polling is disabled");
            return;
        }

        log.info("Starting email polling...");

        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imap");
            props.put("mail.imap.host", emailProperties.getImap().getHost());
            props.put("mail.imap.port", emailProperties.getImap().getPort());
            props.put("mail.imap.ssl.enable", emailProperties.getImap().isSsl());
            props.put("mail.imap.timeout", "30000");
            props.put("mail.imap.connectiontimeout", "30000");

            Session session = Session.getInstance(props, null);
            Store store = session.getStore("imap");

            log.debug("Connecting to IMAP server: {}:{}",
                emailProperties.getImap().getHost(),
                emailProperties.getImap().getPort());

            store.connect(
                emailProperties.getImap().getHost(),
                emailProperties.getImap().getUsername(),
                emailProperties.getImap().getPassword()
            );

            Folder inbox = store.getFolder(emailProperties.getImap().getFolder());
            inbox.open(Folder.READ_WRITE);

            // Fetch unread messages
            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            log.info("Found {} unread emails", messages.length);

            int processedCount = 0;
            int maxMessages = emailProperties.getImap().getMaxMessagesPerPoll();

            for (Message message : messages) {
                if (processedCount >= maxMessages) {
                    log.info("Reached max messages limit ({}) for this poll", maxMessages);
                    break;
                }

                try {
                    processIncomingEmail(message);
                    processedCount++;

                    if (emailProperties.getImap().isMarkAsRead()) {
                        message.setFlag(Flags.Flag.SEEN, true);
                    }

                    if (emailProperties.getImap().isDeleteAfterProcessing()) {
                        message.setFlag(Flags.Flag.DELETED, true);
                    }
                } catch (Exception e) {
                    log.error("Error processing email: {}", e.getMessage(), e);
                }
            }

            inbox.close(true); // true = expunge deleted messages
            store.close();

            log.info("Email polling completed. Processed {} emails", processedCount);

        } catch (MessagingException e) {
            log.error("Error polling emails: {}", e.getMessage(), e);
        }
    }

    /**
     * Process a single incoming email
     */
    private void processIncomingEmail(Message message) throws MessagingException {
        log.info("Processing incoming email...");

        // Extract message ID
        String[] messageIdHeaders = message.getHeader("Message-ID");
        String messageId = (messageIdHeaders != null && messageIdHeaders.length > 0)
            ? messageIdHeaders[0] : null;

        // Check if already processed
        if (messageId != null && emailMessageRepository.existsByMessageId(messageId)) {
            log.debug("Email already processed: {}", messageId);
            return;
        }

        // Extract email data
        String fromAddress = extractEmailAddress(message.getFrom());
        String fromName = extractDisplayName(message.getFrom());
        String toAddresses = extractEmailAddresses(message.getRecipients(Message.RecipientType.TO));
        String ccAddresses = extractEmailAddresses(message.getRecipients(Message.RecipientType.CC));
        String subject = message.getSubject();
        String bodyText = extractTextContent(message);
        String bodyHtml = extractHtmlContent(message);

        // Extract threading headers
        String[] inReplyToHeaders = message.getHeader("In-Reply-To");
        String inReplyTo = (inReplyToHeaders != null && inReplyToHeaders.length > 0)
            ? inReplyToHeaders[0] : null;

        String[] referencesHeaders = message.getHeader("References");
        String references = (referencesHeaders != null && referencesHeaders.length > 0)
            ? referencesHeaders[0] : null;

        // Create EmailMessage entity
        EmailMessage emailMessage = EmailMessage.builder()
            .messageId(messageId)
            .fromAddress(fromAddress)
            .fromName(fromName)
            .toAddresses(toAddresses)
            .ccAddresses(ccAddresses)
            .subject(subject)
            .bodyText(bodyText)
            .bodyHtml(bodyHtml)
            .direction(EmailMessage.EmailDirection.INBOUND)
            .status(EmailMessage.EmailStatus.RECEIVED)
            .receivedAt(message.getReceivedDate() != null
                ? LocalDateTime.ofInstant(message.getReceivedDate().toInstant(), ZoneId.systemDefault())
                : LocalDateTime.now())
            .hasAttachments(hasAttachments(message))
            .attachmentCount(countAttachments(message))
            .inReplyTo(inReplyTo)
            .references(references)
            .build();

        emailMessage = emailMessageRepository.save(emailMessage);

        log.info("Saved email message: {} from {}", subject, fromAddress);

        // Process the email (create ticket or add comment)
        emailProcessorService.processEmail(emailMessage);
    }

    /**
     * Extract email address from Address array
     */
    private String extractEmailAddress(Address[] addresses) {
        if (addresses == null || addresses.length == 0) return null;
        if (addresses[0] instanceof InternetAddress) {
            return ((InternetAddress) addresses[0]).getAddress();
        }
        return addresses[0].toString();
    }

    /**
     * Extract display name from Address array
     */
    private String extractDisplayName(Address[] addresses) {
        if (addresses == null || addresses.length == 0) return null;
        if (addresses[0] instanceof InternetAddress) {
            String personal = ((InternetAddress) addresses[0]).getPersonal();
            return personal != null ? personal : extractEmailAddress(addresses);
        }
        return null;
    }

    /**
     * Extract comma-separated email addresses
     */
    private String extractEmailAddresses(Address[] addresses) {
        if (addresses == null || addresses.length == 0) return null;
        return Arrays.stream(addresses)
            .map(addr -> addr instanceof InternetAddress
                ? ((InternetAddress) addr).getAddress()
                : addr.toString())
            .collect(Collectors.joining(", "));
    }

    /**
     * Extract plain text content from message
     */
    private String extractTextContent(Message message) {
        try {
            Object content = message.getContent();
            if (content instanceof String) {
                return (String) content;
            } else if (content instanceof MimeMultipart) {
                return extractTextFromMultipart((MimeMultipart) content);
            }
        } catch (Exception e) {
            log.error("Error extracting text content: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Extract HTML content from message
     */
    private String extractHtmlContent(Message message) {
        try {
            Object content = message.getContent();
            if (content instanceof MimeMultipart) {
                return extractHtmlFromMultipart((MimeMultipart) content);
            }
        } catch (Exception e) {
            log.error("Error extracting HTML content: {}", e.getMessage());
        }
        return null;
    }

    private String extractTextFromMultipart(MimeMultipart multipart) throws Exception {
        StringBuilder result = new StringBuilder();
        int count = multipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append(bodyPart.getContent().toString());
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(extractTextFromMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

    private String extractHtmlFromMultipart(MimeMultipart multipart) throws Exception {
        int count = multipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = multipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/html")) {
                return bodyPart.getContent().toString();
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                String html = extractHtmlFromMultipart((MimeMultipart) bodyPart.getContent());
                if (html != null) return html;
            }
        }
        return null;
    }

    private boolean hasAttachments(Message message) {
        try {
            Object content = message.getContent();
            if (content instanceof MimeMultipart) {
                MimeMultipart multipart = (MimeMultipart) content;
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error checking attachments: {}", e.getMessage());
        }
        return false;
    }

    private int countAttachments(Message message) {
        int count = 0;
        try {
            Object content = message.getContent();
            if (content instanceof MimeMultipart) {
                MimeMultipart multipart = (MimeMultipart) content;
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                        count++;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error counting attachments: {}", e.getMessage());
        }
        return count;
    }
}
