package io.greenwhite.servicedesk.channel.telegram;

import io.greenwhite.servicedesk.channel.telegram.config.TelegramBotProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TelegramWebhookController
 */
@ExtendWith(MockitoExtension.class)
class TelegramWebhookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TelegramBotProperties properties;

    @Mock
    private TelegramMessageHandler messageHandler;

    @Mock
    private TelegramBotService botService;

    private TelegramWebhookController controller;

    @BeforeEach
    void setUp() {
        controller = new TelegramWebhookController(properties, messageHandler, botService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Should handle webhook POST request")
    void testHandleWebhook() throws Exception {
        // Given
        String updateJson = """
            {
                "update_id": 123456789,
                "message": {
                    "message_id": 1,
                    "from": {
                        "id": 123456,
                        "is_bot": false,
                        "first_name": "Test",
                        "username": "testuser"
                    },
                    "chat": {
                        "id": 123456,
                        "type": "private",
                        "first_name": "Test",
                        "username": "testuser"
                    },
                    "date": 1609459200,
                    "text": "/start"
                }
            }
            """;

        SendMessage response = SendMessage.builder()
            .chatId("123456")
            .text("Welcome!")
            .build();

        when(messageHandler.handleUpdate(any(Update.class))).thenReturn(response);
        when(botService.execute(any(SendMessage.class))).thenReturn(new Message());

        // When & Then
        mockMvc.perform(post("/api/v1/telegram/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
            .andExpect(status().isOk());

        verify(messageHandler).handleUpdate(any(Update.class));
        verify(botService).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("Should handle webhook with null response")
    void testHandleWebhookWithNullResponse() throws Exception {
        // Given
        String updateJson = """
            {
                "update_id": 123456789,
                "message": {
                    "message_id": 1,
                    "from": {
                        "id": 123456,
                        "is_bot": false,
                        "first_name": "Test"
                    },
                    "chat": {
                        "id": 123456,
                        "type": "private"
                    },
                    "date": 1609459200,
                    "text": "random text"
                }
            }
            """;

        when(messageHandler.handleUpdate(any(Update.class))).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/api/v1/telegram/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
            .andExpect(status().isOk());

        verify(messageHandler).handleUpdate(any(Update.class));
        verify(botService, never()).execute(any(SendMessage.class));
    }

    @Test
    @DisplayName("Should return 200 even when handler throws exception")
    void testHandleWebhookWithException() throws Exception {
        // Given
        String updateJson = """
            {
                "update_id": 123456789,
                "message": {
                    "message_id": 1,
                    "from": {
                        "id": 123456,
                        "is_bot": false,
                        "first_name": "Test"
                    },
                    "chat": {
                        "id": 123456,
                        "type": "private"
                    },
                    "date": 1609459200,
                    "text": "/start"
                }
            }
            """;

        when(messageHandler.handleUpdate(any(Update.class)))
            .thenThrow(new RuntimeException("Test exception"));

        // When & Then - Should still return 200 to prevent Telegram retries
        mockMvc.perform(post("/api/v1/telegram/webhook")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should return health status")
    void testHealthCheck() throws Exception {
        // Given
        when(botService.isConfigured()).thenReturn(true);
        when(properties.getBotUsername()).thenReturn("test_bot");
        when(properties.getWebhookUrl()).thenReturn("https://example.com/webhook");
        when(properties.isEnabled()).thenReturn(true);

        // When & Then
        mockMvc.perform(get("/api/v1/telegram/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.botUsername").value("test_bot"))
            .andExpect(jsonPath("$.webhookUrl").value("https://example.com/webhook"))
            .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @DisplayName("Should return NOT_CONFIGURED status when bot is not configured")
    void testHealthCheckNotConfigured() throws Exception {
        // Given
        when(botService.isConfigured()).thenReturn(false);
        when(properties.getBotUsername()).thenReturn(null);
        when(properties.getWebhookUrl()).thenReturn(null);
        when(properties.isEnabled()).thenReturn(false);

        // When & Then
        mockMvc.perform(get("/api/v1/telegram/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("NOT_CONFIGURED"));
    }

    @Test
    @DisplayName("Should return bot info")
    void testGetBotInfo() throws Exception {
        // Given
        when(properties.getBotUsername()).thenReturn("test_bot");
        when(properties.getWebhookUrl()).thenReturn("https://example.com/webhook");
        when(properties.isEnabled()).thenReturn(true);
        when(properties.getDefaultProjectKey()).thenReturn("DESK");

        // When & Then
        mockMvc.perform(get("/api/v1/telegram/info"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("test_bot"))
            .andExpect(jsonPath("$.webhookUrl").value("https://example.com/webhook"))
            .andExpect(jsonPath("$.enabled").value(true))
            .andExpect(jsonPath("$.defaultProjectKey").value("DESK"));
    }
}
