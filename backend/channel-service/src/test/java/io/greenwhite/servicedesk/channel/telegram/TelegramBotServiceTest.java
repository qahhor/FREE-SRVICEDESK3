package io.greenwhite.servicedesk.channel.telegram;

import io.greenwhite.servicedesk.channel.telegram.config.TelegramBotProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TelegramBotService
 */
@ExtendWith(MockitoExtension.class)
class TelegramBotServiceTest {

    @Mock
    private TelegramBotProperties properties;

    @Mock
    private TelegramMessageHandler messageHandler;

    @BeforeEach
    void setUp() {
        lenient().when(properties.getBotToken()).thenReturn("test_token");
        lenient().when(properties.getBotUsername()).thenReturn("test_bot");
    }

    @Test
    @DisplayName("Should return correct bot username")
    void testGetBotUsername() {
        // Given
        when(properties.getBotToken()).thenReturn("test_token");
        when(properties.getBotUsername()).thenReturn("test_bot");
        
        TelegramBotService botService = new TelegramBotService(properties, messageHandler);

        // Then
        assertThat(botService.getBotUsername()).isEqualTo("test_bot");
    }

    @Test
    @DisplayName("Should check if bot is configured correctly")
    void testIsConfigured() {
        // Given - properly configured
        when(properties.getBotToken()).thenReturn("valid_token");
        when(properties.getBotUsername()).thenReturn("valid_bot");
        
        TelegramBotService botService = new TelegramBotService(properties, messageHandler);

        // Then
        assertThat(botService.isConfigured()).isTrue();
    }

    @Test
    @DisplayName("Should return false when bot token is null")
    void testIsConfiguredWithNullToken() {
        // Given
        when(properties.getBotToken()).thenReturn(null);
        when(properties.getBotUsername()).thenReturn("valid_bot");
        
        TelegramBotService botService = new TelegramBotService(properties, messageHandler);

        // Then
        assertThat(botService.isConfigured()).isFalse();
    }

    @Test
    @DisplayName("Should return false when bot token is empty")
    void testIsConfiguredWithEmptyToken() {
        // Given
        when(properties.getBotToken()).thenReturn("");
        when(properties.getBotUsername()).thenReturn("valid_bot");
        
        TelegramBotService botService = new TelegramBotService(properties, messageHandler);

        // Then
        assertThat(botService.isConfigured()).isFalse();
    }

    @Test
    @DisplayName("Should return false when bot username is null")
    void testIsConfiguredWithNullUsername() {
        // Given
        when(properties.getBotToken()).thenReturn("valid_token");
        when(properties.getBotUsername()).thenReturn(null);
        
        TelegramBotService botService = new TelegramBotService(properties, messageHandler);

        // Then
        assertThat(botService.isConfigured()).isFalse();
    }

    @Test
    @DisplayName("Should return false when bot username is empty")
    void testIsConfiguredWithEmptyUsername() {
        // Given
        when(properties.getBotToken()).thenReturn("valid_token");
        when(properties.getBotUsername()).thenReturn("");
        
        TelegramBotService botService = new TelegramBotService(properties, messageHandler);

        // Then
        assertThat(botService.isConfigured()).isFalse();
    }
}
