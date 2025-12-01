package io.greenwhite.servicedesk.channel.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Email configuration properties
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "channel.email")
public class EmailProperties {

    private boolean enabled = true;

    // SMTP Configuration (Outgoing)
    private Smtp smtp = new Smtp();

    // IMAP Configuration (Incoming)
    private Imap imap = new Imap();

    // Processing Configuration
    private Processing processing = new Processing();

    @Data
    public static class Smtp {
        private String host = "smtp.gmail.com";
        private int port = 587;
        private String username;
        private String password;
        private boolean auth = true;
        private boolean starttlsEnable = true;
        private String fromAddress;
        private String fromName = "Service Desk";
    }

    @Data
    public static class Imap {
        private String host = "imap.gmail.com";
        private int port = 993;
        private String username;
        private String password;
        private String folder = "INBOX";
        private boolean ssl = true;
        private int pollIntervalSeconds = 60;
        private int maxMessagesPerPoll = 10;
        private boolean markAsRead = true;
        private boolean deleteAfterProcessing = false;
    }

    @Data
    public static class Processing {
        private boolean autoCreateTicket = true;
        private String defaultProjectKey = "DESK";
        private String defaultPriority = "MEDIUM";
        private int maxSubjectLength = 200;
        private int maxBodyLength = 10000;
        private boolean extractAttachments = true;
    }
}
