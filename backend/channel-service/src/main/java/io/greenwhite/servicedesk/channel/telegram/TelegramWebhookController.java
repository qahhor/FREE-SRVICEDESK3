package io.greenwhite.servicedesk.channel.telegram;

import io.greenwhite.servicedesk.channel.telegram.config.TelegramBotProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Controller for handling Telegram webhook requests
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/telegram")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "channel.telegram", name = "enabled", havingValue = "true")
public class TelegramWebhookController {

    private final TelegramBotProperties properties;
    private final TelegramMessageHandler messageHandler;
    private final TelegramBotService botService;

    /**
     * Webhook endpoint for receiving Telegram updates
     * Called by Telegram servers when new messages arrive
     */
    @PostMapping("/webhook")
    public ResponseEntity<BotApiMethod<?>> handleWebhook(@RequestBody Update update) {
        log.debug("Received webhook update: {}", update.getUpdateId());

        try {
            SendMessage response = messageHandler.handleUpdate(update);
            
            if (response != null) {
                // Execute the response through the bot service
                botService.execute(response);
            }
            
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error processing webhook update: {}", e.getMessage(), e);
            return ResponseEntity.ok().build(); // Always return 200 to prevent Telegram retries
        }
    }

    /**
     * Health check endpoint for the Telegram bot
     */
    @GetMapping("/health")
    public ResponseEntity<TelegramHealthResponse> healthCheck() {
        boolean configured = botService.isConfigured();
        
        return ResponseEntity.ok(TelegramHealthResponse.builder()
            .status(configured ? "UP" : "NOT_CONFIGURED")
            .botUsername(properties.getBotUsername())
            .webhookUrl(properties.getWebhookUrl())
            .enabled(properties.isEnabled())
            .build());
    }

    /**
     * Get bot info endpoint
     */
    @GetMapping("/info")
    public ResponseEntity<TelegramBotInfo> getBotInfo() {
        return ResponseEntity.ok(TelegramBotInfo.builder()
            .username(properties.getBotUsername())
            .webhookUrl(properties.getWebhookUrl())
            .enabled(properties.isEnabled())
            .defaultProjectKey(properties.getDefaultProjectKey())
            .build());
    }

    /**
     * Health response DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TelegramHealthResponse {
        private String status;
        private String botUsername;
        private String webhookUrl;
        private boolean enabled;
    }

    /**
     * Bot info DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TelegramBotInfo {
        private String username;
        private String webhookUrl;
        private boolean enabled;
        private String defaultProjectKey;
    }
}
