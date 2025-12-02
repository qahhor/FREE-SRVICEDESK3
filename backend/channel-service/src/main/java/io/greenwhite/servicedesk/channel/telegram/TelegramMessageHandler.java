package io.greenwhite.servicedesk.channel.telegram;

import io.greenwhite.servicedesk.channel.telegram.config.TelegramBotProperties;
import io.greenwhite.servicedesk.channel.telegram.model.TelegramChat;
import io.greenwhite.servicedesk.channel.telegram.model.TelegramMessage;
import io.greenwhite.servicedesk.channel.telegram.repository.TelegramChatRepository;
import io.greenwhite.servicedesk.channel.telegram.repository.TelegramMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for handling incoming Telegram messages and routing them appropriately
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "channel.telegram", name = "enabled", havingValue = "true")
public class TelegramMessageHandler {

    private final TelegramBotProperties properties;
    private final TelegramChatRepository chatRepository;
    private final TelegramMessageRepository messageRepository;
    private final TelegramKeyboardBuilder keyboardBuilder;

    /**
     * Process incoming update from Telegram
     */
    @Transactional
    public SendMessage handleUpdate(Update update) {
        if (update.hasMessage()) {
            return handleMessage(update);
        } else if (update.hasCallbackQuery()) {
            return handleCallbackQuery(update);
        }
        return null;
    }

    /**
     * Handle incoming text/media message
     */
    private SendMessage handleMessage(Update update) {
        org.telegram.telegrambots.meta.api.objects.Message message = update.getMessage();
        Long chatId = message.getChatId();
        User from = message.getFrom();

        // Get or create chat
        TelegramChat chat = getOrCreateChat(chatId, from);

        // Check if command
        if (message.hasText() && message.getText().startsWith("/")) {
            return handleCommand(chat, message.getText());
        }

        // Store incoming message
        TelegramMessage telegramMessage = storeIncomingMessage(chat, message);

        // Handle based on conversation state
        return handleConversationState(chat, message, telegramMessage);
    }

    /**
     * Handle bot commands
     */
    private SendMessage handleCommand(TelegramChat chat, String text) {
        String command = text.split("\\s+")[0].toLowerCase();
        String args = text.length() > command.length() ? text.substring(command.length()).trim() : "";

        return switch (command) {
            case "/start" -> handleStartCommand(chat);
            case "/help" -> handleHelpCommand(chat);
            case "/new" -> handleNewTicketCommand(chat);
            case "/status" -> handleStatusCommand(chat, args);
            case "/my_tickets" -> handleMyTicketsCommand(chat);
            case "/cancel" -> handleCancelCommand(chat);
            default -> createReply(chat.getChatId(), "Unknown command. Use /help to see available commands.");
        };
    }

    /**
     * Handle /start command
     */
    private SendMessage handleStartCommand(TelegramChat chat) {
        chat.setCurrentState(TelegramChat.ConversationState.IDLE);
        chatRepository.save(chat);

        return SendMessage.builder()
            .chatId(chat.getChatId())
            .text(properties.getWelcomeMessage())
            .replyMarkup(keyboardBuilder.buildMainMenuKeyboard())
            .build();
    }

    /**
     * Handle /help command
     */
    private SendMessage handleHelpCommand(TelegramChat chat) {
        return createReply(chat.getChatId(), properties.getHelpMessage());
    }

    /**
     * Handle /new command - start ticket creation
     */
    private SendMessage handleNewTicketCommand(TelegramChat chat) {
        chat.setCurrentState(TelegramChat.ConversationState.AWAITING_TICKET_SUBJECT);
        chat.setPendingTicketSubject(null);
        chat.setPendingTicketPriority(null);
        chatRepository.save(chat);

        return SendMessage.builder()
            .chatId(chat.getChatId())
            .text("📝 *Create New Ticket*\n\nPlease enter the subject/title of your issue:")
            .parseMode(ParseMode.MARKDOWN)
            .replyMarkup(keyboardBuilder.buildCancelKeyboard())
            .build();
    }

    /**
     * Handle /status command
     */
    private SendMessage handleStatusCommand(TelegramChat chat, String ticketNumber) {
        if (ticketNumber.isEmpty()) {
            return createReply(chat.getChatId(), "Please provide a ticket number.\nUsage: /status DESK-123");
        }

        // TODO: Call Ticket Service to get ticket status
        String statusMessage = String.format(
            "🎫 *Ticket %s*\n\n" +
            "Status: _Checking..._\n\n" +
            "_Ticket service integration pending_",
            ticketNumber
        );

        return SendMessage.builder()
            .chatId(chat.getChatId())
            .text(statusMessage)
            .parseMode(ParseMode.MARKDOWN)
            .build();
    }

    /**
     * Handle /my_tickets command
     */
    private SendMessage handleMyTicketsCommand(TelegramChat chat) {
        // TODO: Call Ticket Service to get user's tickets
        String message = "📋 *Your Tickets*\n\n_Ticket service integration pending_";

        return SendMessage.builder()
            .chatId(chat.getChatId())
            .text(message)
            .parseMode(ParseMode.MARKDOWN)
            .replyMarkup(keyboardBuilder.buildTicketStatusFilterKeyboard())
            .build();
    }

    /**
     * Handle /cancel command
     */
    private SendMessage handleCancelCommand(TelegramChat chat) {
        chat.setCurrentState(TelegramChat.ConversationState.IDLE);
        chat.setPendingTicketSubject(null);
        chat.setPendingTicketPriority(null);
        chatRepository.save(chat);

        return SendMessage.builder()
            .chatId(chat.getChatId())
            .text("Operation cancelled. How can I help you?")
            .replyMarkup(keyboardBuilder.buildMainMenuKeyboard())
            .build();
    }

    /**
     * Handle callback queries from inline keyboards
     */
    private SendMessage handleCallbackQuery(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        TelegramChat chat = chatRepository.findByChatId(chatId)
            .orElseGet(() -> getOrCreateChat(chatId, update.getCallbackQuery().getFrom()));

        // Parse callback data
        if (callbackData.startsWith("action:")) {
            return handleActionCallback(chat, callbackData.substring(7));
        } else if (callbackData.startsWith("priority:")) {
            return handlePriorityCallback(chat, callbackData.substring(9));
        } else if (callbackData.startsWith("filter:")) {
            return handleFilterCallback(chat, callbackData.substring(7));
        } else if (callbackData.startsWith("ticket:")) {
            return handleTicketCallback(chat, callbackData.substring(7));
        }

        return null;
    }

    /**
     * Handle action callbacks
     */
    private SendMessage handleActionCallback(TelegramChat chat, String action) {
        return switch (action) {
            case "new_ticket" -> handleNewTicketCommand(chat);
            case "check_status" -> createReply(chat.getChatId(), "Please enter the ticket number to check:");
            case "my_tickets" -> handleMyTicketsCommand(chat);
            case "help" -> handleHelpCommand(chat);
            case "cancel" -> handleCancelCommand(chat);
            default -> null;
        };
    }

    /**
     * Handle priority selection callback
     */
    private SendMessage handlePriorityCallback(TelegramChat chat, String priority) {
        if (chat.getCurrentState() == TelegramChat.ConversationState.AWAITING_TICKET_PRIORITY) {
            chat.setPendingTicketPriority(priority);
            chat.setCurrentState(TelegramChat.ConversationState.AWAITING_TICKET_CONFIRMATION);
            chatRepository.save(chat);

            String confirmMessage = String.format(
                "📝 *Confirm Ticket Creation*\n\n" +
                "*Subject:* %s\n" +
                "*Priority:* %s\n\n" +
                "Ready to create this ticket?",
                chat.getPendingTicketSubject(),
                priority
            );

            return SendMessage.builder()
                .chatId(chat.getChatId())
                .text(confirmMessage)
                .parseMode(ParseMode.MARKDOWN)
                .replyMarkup(keyboardBuilder.buildConfirmationKeyboard("action:confirm_ticket", "action:cancel"))
                .build();
        }
        return null;
    }

    /**
     * Handle filter callbacks
     */
    private SendMessage handleFilterCallback(TelegramChat chat, String filter) {
        // TODO: Apply filter and fetch tickets
        return createReply(chat.getChatId(), "Filter applied: " + filter + "\n_Ticket service integration pending_");
    }

    /**
     * Handle ticket-specific callbacks
     */
    private SendMessage handleTicketCallback(TelegramChat chat, String ticketAction) {
        // Format: ticketId:action
        String[] parts = ticketAction.split(":");
        if (parts.length >= 2) {
            String ticketId = parts[0];
            String action = parts[1];
            // TODO: Handle ticket actions
            return createReply(chat.getChatId(), "Action: " + action + " on ticket: " + ticketId);
        }
        return null;
    }

    /**
     * Handle conversation state for multi-step operations
     */
    private SendMessage handleConversationState(TelegramChat chat, 
                                                  org.telegram.telegrambots.meta.api.objects.Message message,
                                                  TelegramMessage telegramMessage) {
        String text = message.hasText() ? message.getText() : "";

        return switch (chat.getCurrentState()) {
            case AWAITING_TICKET_SUBJECT -> {
                chat.setPendingTicketSubject(text);
                chat.setCurrentState(TelegramChat.ConversationState.AWAITING_TICKET_DESCRIPTION);
                chatRepository.save(chat);
                yield createReply(chat.getChatId(), 
                    "Great! Now please describe your issue in detail.\n\n" +
                    "You can also send photos or documents to attach them to your ticket.");
            }
            case AWAITING_TICKET_DESCRIPTION -> {
                // Store description with the message
                telegramMessage.setProcessed(false); // Will be processed when ticket is created
                messageRepository.save(telegramMessage);
                
                chat.setCurrentState(TelegramChat.ConversationState.AWAITING_TICKET_PRIORITY);
                chatRepository.save(chat);
                
                yield SendMessage.builder()
                    .chatId(chat.getChatId())
                    .text("Please select the priority of your issue:")
                    .replyMarkup(keyboardBuilder.buildPriorityKeyboard())
                    .build();
            }
            case AWAITING_TICKET_CONFIRMATION -> {
                // If user sends text during confirmation, treat as additional info
                yield createReply(chat.getChatId(), "Please use the buttons above to confirm or cancel.");
            }
            case IDLE -> {
                // Regular message - could be a reply to an existing ticket
                // TODO: Check if there's an active ticket conversation and add as comment
                yield SendMessage.builder()
                    .chatId(chat.getChatId())
                    .text("Thanks for your message! How can I help you today?")
                    .replyMarkup(keyboardBuilder.buildMainMenuKeyboard())
                    .build();
            }
            default -> createReply(chat.getChatId(), "I didn't understand that. Use /help to see available commands.");
        };
    }

    /**
     * Get or create Telegram chat entity
     */
    private TelegramChat getOrCreateChat(Long chatId, User user) {
        return chatRepository.findByChatId(chatId)
            .orElseGet(() -> {
                TelegramChat newChat = TelegramChat.builder()
                    .chatId(chatId)
                    .username(user.getUserName())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .languageCode(user.getLanguageCode())
                    .isBlocked(false)
                    .currentState(TelegramChat.ConversationState.IDLE)
                    .build();
                return chatRepository.save(newChat);
            });
    }

    /**
     * Store incoming message
     */
    private TelegramMessage storeIncomingMessage(TelegramChat chat, 
                                                   org.telegram.telegrambots.meta.api.objects.Message message) {
        TelegramMessage.MessageType type = determineMessageType(message);
        String content = extractContent(message);
        String fileId = extractFileId(message);

        TelegramMessage telegramMessage = TelegramMessage.builder()
            .chatId(chat.getChatId())
            .messageId(message.getMessageId().longValue())
            .direction(TelegramMessage.MessageDirection.INCOMING)
            .messageType(type)
            .content(content)
            .fileId(fileId)
            .processed(false)
            .build();

        return messageRepository.save(telegramMessage);
    }

    /**
     * Determine message type
     */
    private TelegramMessage.MessageType determineMessageType(org.telegram.telegrambots.meta.api.objects.Message message) {
        if (message.hasPhoto()) return TelegramMessage.MessageType.PHOTO;
        if (message.hasDocument()) return TelegramMessage.MessageType.DOCUMENT;
        if (message.hasVoice()) return TelegramMessage.MessageType.VOICE;
        if (message.hasVideo()) return TelegramMessage.MessageType.VIDEO;
        if (message.hasAudio()) return TelegramMessage.MessageType.AUDIO;
        if (message.hasSticker()) return TelegramMessage.MessageType.STICKER;
        if (message.hasContact()) return TelegramMessage.MessageType.CONTACT;
        if (message.hasLocation()) return TelegramMessage.MessageType.LOCATION;
        if (message.hasText() && message.getText().startsWith("/")) return TelegramMessage.MessageType.COMMAND;
        return TelegramMessage.MessageType.TEXT;
    }

    /**
     * Extract content from message
     */
    private String extractContent(org.telegram.telegrambots.meta.api.objects.Message message) {
        if (message.hasText()) return message.getText();
        if (message.getCaption() != null) return message.getCaption();
        return null;
    }

    /**
     * Extract file ID from message
     */
    private String extractFileId(org.telegram.telegrambots.meta.api.objects.Message message) {
        if (message.hasPhoto()) {
            // Get the largest photo
            return message.getPhoto().stream()
                .reduce((first, second) -> second)
                .map(photo -> photo.getFileId())
                .orElse(null);
        }
        if (message.hasDocument()) return message.getDocument().getFileId();
        if (message.hasVoice()) return message.getVoice().getFileId();
        if (message.hasVideo()) return message.getVideo().getFileId();
        if (message.hasAudio()) return message.getAudio().getFileId();
        return null;
    }

    /**
     * Helper to create simple text reply
     */
    private SendMessage createReply(Long chatId, String text) {
        return SendMessage.builder()
            .chatId(chatId)
            .text(text)
            .build();
    }
}
