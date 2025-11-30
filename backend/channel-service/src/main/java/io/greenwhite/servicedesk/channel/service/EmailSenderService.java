package io.greenwhite.servicedesk.channel.service;

import io.greenwhite.servicedesk.channel.config.EmailProperties;
import io.greenwhite.servicedesk.channel.model.EmailMessage;
import io.greenwhite.servicedesk.channel.repository.EmailMessageRepository;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Properties;

/**
 * Service for sending outbound emails
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final EmailProperties emailProperties;
    private final EmailMessageRepository emailMessageRepository;

    /**
     * Send email notification
     */
    @Transactional
    public void sendEmail(String toAddress, String subject, String body, String ticketId) {
        log.info("Sending email to: {}, subject: {}", toAddress, subject);

        EmailMessage emailMessage = EmailMessage.builder()
            .toAddresses(toAddress)
            .fromAddress(emailProperties.getSmtp().getFromAddress())
            .fromName(emailProperties.getSmtp().getFromName())
            .subject(subject)
            .bodyText(body)
            .direction(EmailMessage.EmailDirection.OUTBOUND)
            .status(EmailMessage.EmailStatus.SENDING)
            .ticketId(ticketId)
            .build();

        emailMessage = emailMessageRepository.save(emailMessage);

        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", emailProperties.getSmtp().getHost());
            props.put("mail.smtp.port", emailProperties.getSmtp().getPort());
            props.put("mail.smtp.auth", emailProperties.getSmtp().isAuth());
            props.put("mail.smtp.starttls.enable", emailProperties.getSmtp().isStarttlsEnable());
            props.put("mail.smtp.timeout", "30000");
            props.put("mail.smtp.connectiontimeout", "30000");

            Session session = Session.getInstance(props, new jakarta.mail.Authenticator() {
                @Override
                protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new jakarta.mail.PasswordAuthentication(
                        emailProperties.getSmtp().getUsername(),
                        emailProperties.getSmtp().getPassword()
                    );
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(
                emailProperties.getSmtp().getFromAddress(),
                emailProperties.getSmtp().getFromName()
            ));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toAddress));
            message.setSubject(subject);
            message.setText(body, "UTF-8");

            // Add ticket reference header for threading
            if (ticketId != null) {
                message.addHeader("X-Ticket-ID", ticketId);
            }

            Transport.send(message);

            emailMessage.setStatus(EmailMessage.EmailStatus.SENT);
            emailMessage.setSentAt(LocalDateTime.now());
            emailMessage.setMessageId(message.getMessageID());
            emailMessageRepository.save(emailMessage);

            log.info("Email sent successfully: {}", emailMessage.getId());

        } catch (Exception e) {
            log.error("Error sending email: {}", e.getMessage(), e);
            emailMessage.setStatus(EmailMessage.EmailStatus.FAILED);
            emailMessage.setErrorMessage(e.getMessage());
            emailMessageRepository.save(emailMessage);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Send ticket creation notification
     */
    public void sendTicketCreatedNotification(String toAddress, String ticketNumber, String subject) {
        String emailBody = String.format(
            "Your ticket has been created.\n\n" +
            "Ticket Number: %s\n" +
            "Subject: %s\n\n" +
            "We will respond to your request as soon as possible.\n\n" +
            "Thank you,\n%s",
            ticketNumber, subject, emailProperties.getSmtp().getFromName()
        );

        sendEmail(toAddress, "[" + ticketNumber + "] " + subject, emailBody, ticketNumber);
    }

    /**
     * Send comment notification
     */
    public void sendCommentNotification(String toAddress, String ticketNumber, String commentText) {
        String emailBody = String.format(
            "A new comment has been added to your ticket.\n\n" +
            "Ticket Number: %s\n" +
            "Comment:\n%s\n\n" +
            "Thank you,\n%s",
            ticketNumber, commentText, emailProperties.getSmtp().getFromName()
        );

        sendEmail(toAddress, "[" + ticketNumber + "] New Comment", emailBody, ticketNumber);
    }
}
