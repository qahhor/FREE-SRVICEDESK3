package io.greenwhite.servicedesk.channel.telegram.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Telegram bot configuration properties
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "channel.telegram")
public class TelegramBotProperties {

    /**
     * Whether Telegram integration is enabled
     */
    private boolean enabled = false;

    /**
     * Telegram Bot API token from BotFather
     */
    private String botToken;

    /**
     * Telegram bot username (without @)
     */
    private String botUsername;

    /**
     * Webhook URL for receiving updates
     */
    private String webhookUrl;

    /**
     * Default project key for creating tickets
     */
    private String defaultProjectKey = "DESK";

    /**
     * Maximum text message length
     */
    private int maxMessageLength = 4096;

    /**
     * Welcome message for /start command
     */
    private String welcomeMessage = "Welcome to Service Desk! How can we help you today?";

    /**
     * Help message for /help command
     */
    private String helpMessage = """
        Available commands:
        /start - Start conversation
        /help - Show this help message
        /new - Create a new ticket
        /status <ticket_number> - Check ticket status
        /my_tickets - List your tickets
        /cancel - Cancel current operation
        """;
}
