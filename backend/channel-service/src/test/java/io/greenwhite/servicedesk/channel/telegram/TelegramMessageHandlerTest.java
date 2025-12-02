package io.greenwhite.servicedesk.channel.telegram;

import io.greenwhite.servicedesk.channel.telegram.config.TelegramBotProperties;
import io.greenwhite.servicedesk.channel.telegram.model.TelegramChat;
import io.greenwhite.servicedesk.channel.telegram.model.TelegramMessage;
import io.greenwhite.servicedesk.channel.telegram.repository.TelegramChatRepository;
import io.greenwhite.servicedesk.channel.telegram.repository.TelegramMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TelegramMessageHandler
 */
@ExtendWith(MockitoExtension.class)
class TelegramMessageHandlerTest {

    @Mock
    private TelegramBotProperties properties;

    @Mock
    private TelegramChatRepository chatRepository;

    @Mock
    private TelegramMessageRepository messageRepository;

    @Mock
    private TelegramKeyboardBuilder keyboardBuilder;

    @InjectMocks
    private TelegramMessageHandler messageHandler;

    private TelegramChat testChat;
    private Update testUpdate;

    @BeforeEach
    void setUp() {
        testChat = TelegramChat.builder()
            .chatId(123456L)
            .username("testuser")
            .firstName("Test")
            .lastName("User")
            .isBlocked(false)
            .currentState(TelegramChat.ConversationState.IDLE)
            .build();

        // Create test update
        testUpdate = createTestUpdate(123456L, "/start", 1);
    }

    @Test
    @DisplayName("Should handle /start command and return welcome message")
    void testHandleStartCommand() {
        // Given
        when(properties.getWelcomeMessage()).thenReturn("Welcome to Service Desk!");
        when(chatRepository.findByChatId(123456L)).thenReturn(Optional.of(testChat));
        when(chatRepository.save(any(TelegramChat.class))).thenReturn(testChat);

        // When
        SendMessage response = messageHandler.handleUpdate(testUpdate);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getChatId()).isEqualTo("123456");
        assertThat(response.getText()).isEqualTo("Welcome to Service Desk!");
        verify(chatRepository).save(testChat);
        assertThat(testChat.getCurrentState()).isEqualTo(TelegramChat.ConversationState.IDLE);
    }

    @Test
    @DisplayName("Should handle /help command and return help message")
    void testHandleHelpCommand() {
        // Given
        Update helpUpdate = createTestUpdate(123456L, "/help", 2);
        when(properties.getHelpMessage()).thenReturn("Available commands...");
        when(chatRepository.findByChatId(123456L)).thenReturn(Optional.of(testChat));

        // When
        SendMessage response = messageHandler.handleUpdate(helpUpdate);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getText()).isEqualTo("Available commands...");
    }

    @Test
    @DisplayName("Should handle /new command and set state to AWAITING_TICKET_SUBJECT")
    void testHandleNewTicketCommand() {
        // Given
        Update newUpdate = createTestUpdate(123456L, "/new", 3);
        when(chatRepository.findByChatId(123456L)).thenReturn(Optional.of(testChat));
        when(chatRepository.save(any(TelegramChat.class))).thenReturn(testChat);

        // When
        SendMessage response = messageHandler.handleUpdate(newUpdate);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getText()).contains("Create New Ticket");
        
        ArgumentCaptor<TelegramChat> chatCaptor = ArgumentCaptor.forClass(TelegramChat.class);
        verify(chatRepository).save(chatCaptor.capture());
        assertThat(chatCaptor.getValue().getCurrentState())
            .isEqualTo(TelegramChat.ConversationState.AWAITING_TICKET_SUBJECT);
    }

    @Test
    @DisplayName("Should handle /cancel command and reset state to IDLE")
    void testHandleCancelCommand() {
        // Given
        testChat.setCurrentState(TelegramChat.ConversationState.AWAITING_TICKET_SUBJECT);
        testChat.setPendingTicketSubject("Test subject");
        
        Update cancelUpdate = createTestUpdate(123456L, "/cancel", 4);
        when(chatRepository.findByChatId(123456L)).thenReturn(Optional.of(testChat));
        when(chatRepository.save(any(TelegramChat.class))).thenReturn(testChat);

        // When
        SendMessage response = messageHandler.handleUpdate(cancelUpdate);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getText()).contains("cancelled");
        
        ArgumentCaptor<TelegramChat> chatCaptor = ArgumentCaptor.forClass(TelegramChat.class);
        verify(chatRepository).save(chatCaptor.capture());
        assertThat(chatCaptor.getValue().getCurrentState())
            .isEqualTo(TelegramChat.ConversationState.IDLE);
        assertThat(chatCaptor.getValue().getPendingTicketSubject()).isNull();
    }

    @Test
    @DisplayName("Should handle /status command with ticket number")
    void testHandleStatusCommandWithTicketNumber() {
        // Given
        Update statusUpdate = createTestUpdate(123456L, "/status DESK-123", 5);
        when(chatRepository.findByChatId(123456L)).thenReturn(Optional.of(testChat));

        // When
        SendMessage response = messageHandler.handleUpdate(statusUpdate);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getText()).contains("DESK-123");
    }

    @Test
    @DisplayName("Should handle /status command without ticket number")
    void testHandleStatusCommandWithoutTicketNumber() {
        // Given
        Update statusUpdate = createTestUpdate(123456L, "/status", 6);
        when(chatRepository.findByChatId(123456L)).thenReturn(Optional.of(testChat));

        // When
        SendMessage response = messageHandler.handleUpdate(statusUpdate);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getText()).contains("Please provide a ticket number");
    }

    @Test
    @DisplayName("Should create new chat for unknown user on /start")
    void testCreateNewChatForUnknownUser() {
        // Given
        when(properties.getWelcomeMessage()).thenReturn("Welcome!");
        when(chatRepository.findByChatId(123456L)).thenReturn(Optional.empty());
        when(chatRepository.save(any(TelegramChat.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        SendMessage response = messageHandler.handleUpdate(testUpdate);

        // Then
        assertThat(response).isNotNull();
        
        ArgumentCaptor<TelegramChat> chatCaptor = ArgumentCaptor.forClass(TelegramChat.class);
        verify(chatRepository, times(2)).save(chatCaptor.capture());
        
        TelegramChat savedChat = chatCaptor.getAllValues().get(0);
        assertThat(savedChat.getChatId()).isEqualTo(123456L);
        assertThat(savedChat.getUsername()).isEqualTo("testuser");
        assertThat(savedChat.getFirstName()).isEqualTo("Test");
    }

    @Test
    @DisplayName("Should handle unknown command")
    void testHandleUnknownCommand() {
        // Given
        Update unknownUpdate = createTestUpdate(123456L, "/unknown", 7);
        when(chatRepository.findByChatId(123456L)).thenReturn(Optional.of(testChat));

        // When
        SendMessage response = messageHandler.handleUpdate(unknownUpdate);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getText()).contains("Unknown command");
    }

    @Test
    @DisplayName("Should collect ticket subject when in AWAITING_TICKET_SUBJECT state")
    void testCollectTicketSubject() {
        // Given
        testChat.setCurrentState(TelegramChat.ConversationState.AWAITING_TICKET_SUBJECT);
        Update subjectUpdate = createTestUpdate(123456L, "My printer is not working", 8);
        
        when(chatRepository.findByChatId(123456L)).thenReturn(Optional.of(testChat));
        when(chatRepository.save(any(TelegramChat.class))).thenReturn(testChat);
        when(messageRepository.save(any(TelegramMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        SendMessage response = messageHandler.handleUpdate(subjectUpdate);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getText()).contains("describe your issue");
        
        ArgumentCaptor<TelegramChat> chatCaptor = ArgumentCaptor.forClass(TelegramChat.class);
        verify(chatRepository).save(chatCaptor.capture());
        assertThat(chatCaptor.getValue().getPendingTicketSubject()).isEqualTo("My printer is not working");
        assertThat(chatCaptor.getValue().getCurrentState())
            .isEqualTo(TelegramChat.ConversationState.AWAITING_TICKET_DESCRIPTION);
    }

    @Test
    @DisplayName("Should handle /my_tickets command")
    void testHandleMyTicketsCommand() {
        // Given
        Update ticketsUpdate = createTestUpdate(123456L, "/my_tickets", 9);
        when(chatRepository.findByChatId(123456L)).thenReturn(Optional.of(testChat));

        // When
        SendMessage response = messageHandler.handleUpdate(ticketsUpdate);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getText()).contains("Your Tickets");
    }

    /**
     * Helper method to create test Update objects
     */
    private Update createTestUpdate(Long chatId, String text, Integer messageId) {
        User user = new User();
        user.setId(chatId);
        user.setUserName("testuser");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setLanguageCode("en");

        Chat chat = new Chat();
        chat.setId(chatId);
        chat.setType("private");

        Message message = new Message();
        message.setMessageId(messageId);
        message.setFrom(user);
        message.setChat(chat);
        message.setText(text);

        Update update = new Update();
        update.setUpdateId(messageId);
        update.setMessage(message);

        return update;
    }
}
