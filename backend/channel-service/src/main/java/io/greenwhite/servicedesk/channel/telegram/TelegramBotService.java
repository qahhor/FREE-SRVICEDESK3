package io.greenwhite.servicedesk.channel.telegram;

import io.greenwhite.servicedesk.channel.telegram.config.TelegramBotProperties;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

/**
 * Main Telegram Bot service for handling bot operations
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix = "channel.telegram", name = "enabled", havingValue = "true")
public class TelegramBotService extends TelegramLongPollingBot {

    private final TelegramBotProperties properties;
    private final TelegramMessageHandler messageHandler;

    @Getter
    private final String botUsername;

    public TelegramBotService(TelegramBotProperties properties, 
                               TelegramMessageHandler messageHandler) {
        super(properties.getBotToken());
        this.properties = properties;
        this.messageHandler = messageHandler;
        this.botUsername = properties.getBotUsername();
    }

    @PostConstruct
    public void init() {
        log.info("Telegram bot service initialized. Bot username: {}", botUsername);
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            log.debug("Received update: {}", update.getUpdateId());

            SendMessage response = messageHandler.handleUpdate(update);

            if (response != null) {
                execute(response);
            }

        } catch (TelegramApiException e) {
            log.error("Error executing Telegram API call: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Error processing update: {}", e.getMessage(), e);
            
            // Try to send error message to user
            try {
                Long chatId = extractChatId(update);
                if (chatId != null) {
                    execute(SendMessage.builder()
                        .chatId(chatId)
                        .text("Sorry, an error occurred. Please try again or contact support.")
                        .build());
                }
            } catch (Exception ex) {
                log.error("Failed to send error message: {}", ex.getMessage());
            }
        }
    }

    /**
     * Execute a bot API method and return the result
     */
    public <T extends Serializable, M extends BotApiMethod<T>> T executeMethod(M method) throws TelegramApiException {
        return execute(method);
    }

    /**
     * Send a text message
     */
    public Message sendTextMessage(Long chatId, String text) throws TelegramApiException {
        return execute(SendMessage.builder()
            .chatId(chatId)
            .text(text)
            .build());
    }

    /**
     * Send a photo message
     */
    public Message sendPhotoMessage(SendPhoto sendPhoto) throws TelegramApiException {
        return execute(sendPhoto);
    }

    /**
     * Send a document message
     */
    public Message sendDocumentMessage(SendDocument sendDocument) throws TelegramApiException {
        return execute(sendDocument);
    }

    /**
     * Extract chat ID from update
     */
    private Long extractChatId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getChatId();
        } else if (update.hasEditedMessage()) {
            return update.getEditedMessage().getChatId();
        }
        return null;
    }

    /**
     * Check if bot is properly configured
     */
    public boolean isConfigured() {
        return properties.getBotToken() != null && !properties.getBotToken().isEmpty()
            && properties.getBotUsername() != null && !properties.getBotUsername().isEmpty();
    }
}
