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
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for sending notifications to Telegram users
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "channel.telegram", name = "enabled", havingValue = "true")
public class TelegramNotificationService {

    private final TelegramBotProperties properties;
    private final TelegramChatRepository chatRepository;
    private final TelegramMessageRepository messageRepository;
    private final TelegramBotService botService;
    private final TelegramKeyboardBuilder keyboardBuilder;

    /**
     * Send ticket created notification
     */
    @Transactional
    public void sendTicketCreatedNotification(Long chatId, String ticketNumber, String subject) {
        String message = String.format(
            "✅ *Ticket Created*\n\n" +
            "🎫 *Ticket:* %s\n" +
            "📝 *Subject:* %s\n\n" +
            "We've received your request and will respond as soon as possible.\n" +
            "Use `/status %s` to check the status.",
            ticketNumber, escapeMarkdown(subject), ticketNumber
        );

        sendMessage(chatId, message, ParseMode.MARKDOWN, null);
    }

    /**
     * Send ticket status update notification
     */
    @Transactional
    public void sendTicketStatusUpdate(Long chatId, String ticketNumber, String oldStatus, String newStatus) {
        String emoji = getStatusEmoji(newStatus);
        String message = String.format(
            "%s *Ticket Status Updated*\n\n" +
            "🎫 *Ticket:* %s\n" +
            "📊 *Status:* %s → %s",
            emoji, ticketNumber, oldStatus, newStatus
        );

        sendMessage(chatId, message, ParseMode.MARKDOWN, null);
    }

    /**
     * Send new comment notification
     */
    @Transactional
    public void sendCommentNotification(Long chatId, String ticketNumber, String authorName, String comment) {
        String truncatedComment = comment.length() > 500 
            ? comment.substring(0, 497) + "..." 
            : comment;

        String message = String.format(
            "💬 *New Comment on Ticket %s*\n\n" +
            "👤 *From:* %s\n\n" +
            "%s",
            ticketNumber, escapeMarkdown(authorName), escapeMarkdown(truncatedComment)
        );

        InlineKeyboardMarkup keyboard = keyboardBuilder.buildTicketActionsKeyboard(ticketNumber);
        sendMessage(chatId, message, ParseMode.MARKDOWN, keyboard);
    }

    /**
     * Send agent reply notification
     */
    @Transactional
    public void sendAgentReply(Long chatId, String ticketNumber, String agentName, String reply) {
        String message = String.format(
            "📩 *Agent Reply on Ticket %s*\n\n" +
            "👤 *Agent:* %s\n\n" +
            "%s",
            ticketNumber, escapeMarkdown(agentName), escapeMarkdown(reply)
        );

        InlineKeyboardMarkup keyboard = keyboardBuilder.buildTicketActionsKeyboard(ticketNumber);
        sendMessage(chatId, message, ParseMode.MARKDOWN, keyboard);
    }

    /**
     * Send ticket resolved notification
     */
    @Transactional
    public void sendTicketResolvedNotification(Long chatId, String ticketNumber, String resolution) {
        String message = String.format(
            "✅ *Ticket Resolved*\n\n" +
            "🎫 *Ticket:* %s\n\n" +
            "*Resolution:*\n%s\n\n" +
            "If you need further assistance, please create a new ticket.",
            ticketNumber, escapeMarkdown(resolution != null ? resolution : "Issue has been resolved.")
        );

        sendMessage(chatId, message, ParseMode.MARKDOWN, keyboardBuilder.buildMainMenuKeyboard());
    }

    /**
     * Send SLA breach warning
     */
    @Transactional
    public void sendSlaWarning(Long chatId, String ticketNumber, String slaType, int minutesRemaining) {
        String message = String.format(
            "⚠️ *SLA Warning*\n\n" +
            "🎫 *Ticket:* %s\n" +
            "⏰ *%s SLA breach in:* %d minutes",
            ticketNumber, slaType, minutesRemaining
        );

        sendMessage(chatId, message, ParseMode.MARKDOWN, null);
    }

    /**
     * Send file attachment notification
     */
    @Transactional
    public void sendFileAttachmentNotification(Long chatId, String ticketNumber, String fileName, String attachedBy) {
        String message = String.format(
            "📎 *File Attached to Ticket %s*\n\n" +
            "📄 *File:* %s\n" +
            "👤 *By:* %s",
            ticketNumber, escapeMarkdown(fileName), escapeMarkdown(attachedBy)
        );

        sendMessage(chatId, message, ParseMode.MARKDOWN, null);
    }

    /**
     * Send custom notification message
     */
    @Transactional
    public void sendCustomNotification(Long chatId, String title, String body) {
        String message = String.format("*%s*\n\n%s", escapeMarkdown(title), escapeMarkdown(body));
        sendMessage(chatId, message, ParseMode.MARKDOWN, null);
    }

    /**
     * Send notification by user ID (finds linked Telegram chat)
     */
    @Transactional
    public boolean sendNotificationByUserId(UUID userId, String title, String body) {
        Optional<TelegramChat> chat = chatRepository.findByUserId(userId);
        if (chat.isEmpty() || chat.get().getIsBlocked()) {
            log.debug("No Telegram chat found for user {} or chat is blocked", userId);
            return false;
        }

        sendCustomNotification(chat.get().getChatId(), title, body);
        return true;
    }

    /**
     * Send a message and store it
     */
    private void sendMessage(Long chatId, String text, String parseMode, InlineKeyboardMarkup keyboard) {
        try {
            SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode(parseMode)
                .replyMarkup(keyboard)
                .build();

            var sentMessage = botService.execute(sendMessage);

            // Store outgoing message
            TelegramMessage telegramMessage = TelegramMessage.builder()
                .chatId(chatId)
                .messageId(sentMessage.getMessageId().longValue())
                .direction(TelegramMessage.MessageDirection.OUTGOING)
                .messageType(TelegramMessage.MessageType.TEXT)
                .content(text)
                .processed(true)
                .build();

            messageRepository.save(telegramMessage);

            log.debug("Sent message to chat {}: {}", chatId, text.substring(0, Math.min(50, text.length())));

        } catch (Exception e) {
            log.error("Failed to send message to chat {}: {}", chatId, e.getMessage(), e);
        }
    }

    /**
     * Send photo with caption
     */
    public void sendPhoto(Long chatId, InputFile photo, String caption) {
        try {
            SendPhoto sendPhoto = SendPhoto.builder()
                .chatId(chatId)
                .photo(photo)
                .caption(caption)
                .parseMode(ParseMode.MARKDOWN)
                .build();

            var sentMessage = botService.execute(sendPhoto);

            TelegramMessage telegramMessage = TelegramMessage.builder()
                .chatId(chatId)
                .messageId(sentMessage.getMessageId().longValue())
                .direction(TelegramMessage.MessageDirection.OUTGOING)
                .messageType(TelegramMessage.MessageType.PHOTO)
                .content(caption)
                .processed(true)
                .build();

            messageRepository.save(telegramMessage);

        } catch (Exception e) {
            log.error("Failed to send photo to chat {}: {}", chatId, e.getMessage(), e);
        }
    }

    /**
     * Send document
     */
    public void sendDocument(Long chatId, InputFile document, String caption) {
        try {
            SendDocument sendDocument = SendDocument.builder()
                .chatId(chatId)
                .document(document)
                .caption(caption)
                .parseMode(ParseMode.MARKDOWN)
                .build();

            var sentMessage = botService.execute(sendDocument);

            TelegramMessage telegramMessage = TelegramMessage.builder()
                .chatId(chatId)
                .messageId(sentMessage.getMessageId().longValue())
                .direction(TelegramMessage.MessageDirection.OUTGOING)
                .messageType(TelegramMessage.MessageType.DOCUMENT)
                .content(caption)
                .processed(true)
                .build();

            messageRepository.save(telegramMessage);

        } catch (Exception e) {
            log.error("Failed to send document to chat {}: {}", chatId, e.getMessage(), e);
        }
    }

    /**
     * Get emoji for status
     */
    private String getStatusEmoji(String status) {
        return switch (status.toUpperCase()) {
            case "NEW" -> "🆕";
            case "OPEN" -> "🔄";
            case "IN_PROGRESS" -> "⏳";
            case "PENDING" -> "⏸️";
            case "RESOLVED" -> "✅";
            case "CLOSED" -> "🔒";
            default -> "📊";
        };
    }

    /**
     * Escape special characters for Markdown
     */
    private String escapeMarkdown(String text) {
        if (text == null) return "";
        return text
            .replace("_", "\\_")
            .replace("*", "\\*")
            .replace("[", "\\[")
            .replace("]", "\\]")
            .replace("(", "\\(")
            .replace(")", "\\)")
            .replace("~", "\\~")
            .replace("`", "\\`")
            .replace(">", "\\>")
            .replace("#", "\\#")
            .replace("+", "\\+")
            .replace("-", "\\-")
            .replace("=", "\\=")
            .replace("|", "\\|")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace(".", "\\.")
            .replace("!", "\\!");
    }
}
