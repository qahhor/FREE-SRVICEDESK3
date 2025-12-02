package io.greenwhite.servicedesk.channel.telegram;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for Telegram inline and reply keyboards
 */
@Component
public class TelegramKeyboardBuilder {

    /**
     * Build main menu keyboard
     */
    public InlineKeyboardMarkup buildMainMenuKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        // First row - Create Ticket
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("📝 Create Ticket", "action:new_ticket"));
        rows.add(row1);

        // Second row - Check Status and My Tickets
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton("🔍 Check Status", "action:check_status"));
        row2.add(createButton("📋 My Tickets", "action:my_tickets"));
        rows.add(row2);

        // Third row - Help
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createButton("❓ Help", "action:help"));
        rows.add(row3);

        return InlineKeyboardMarkup.builder()
            .keyboard(rows)
            .build();
    }

    /**
     * Build priority selection keyboard
     */
    public InlineKeyboardMarkup buildPriorityKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("🔴 Critical", "priority:CRITICAL"));
        row1.add(createButton("🟠 High", "priority:HIGH"));
        rows.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton("🟡 Medium", "priority:MEDIUM"));
        row2.add(createButton("🟢 Low", "priority:LOW"));
        rows.add(row2);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createButton("❌ Cancel", "action:cancel"));
        rows.add(row3);

        return InlineKeyboardMarkup.builder()
            .keyboard(rows)
            .build();
    }

    /**
     * Build confirmation keyboard
     */
    public InlineKeyboardMarkup buildConfirmationKeyboard(String confirmAction, String cancelAction) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createButton("✅ Confirm", confirmAction));
        row.add(createButton("❌ Cancel", cancelAction));
        rows.add(row);

        return InlineKeyboardMarkup.builder()
            .keyboard(rows)
            .build();
    }

    /**
     * Build ticket status keyboard for selecting status to filter
     */
    public InlineKeyboardMarkup buildTicketStatusFilterKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("🆕 New", "filter:status:NEW"));
        row1.add(createButton("🔄 Open", "filter:status:OPEN"));
        rows.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton("⏳ Pending", "filter:status:PENDING"));
        row2.add(createButton("✅ Resolved", "filter:status:RESOLVED"));
        rows.add(row2);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createButton("📊 All", "filter:status:ALL"));
        rows.add(row3);

        return InlineKeyboardMarkup.builder()
            .keyboard(rows)
            .build();
    }

    /**
     * Build ticket actions keyboard
     */
    public InlineKeyboardMarkup buildTicketActionsKeyboard(String ticketId) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createButton("💬 Add Comment", "ticket:" + ticketId + ":comment"));
        row1.add(createButton("📎 Add File", "ticket:" + ticketId + ":file"));
        rows.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createButton("🔄 Refresh", "ticket:" + ticketId + ":refresh"));
        row2.add(createButton("🔙 Back", "action:my_tickets"));
        rows.add(row2);

        return InlineKeyboardMarkup.builder()
            .keyboard(rows)
            .build();
    }

    /**
     * Build reply keyboard with common options
     */
    public ReplyKeyboardMarkup buildReplyKeyboard() {
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("📝 New Ticket"));
        row1.add(new KeyboardButton("📋 My Tickets"));
        keyboard.add(row1);

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("🔍 Check Status"));
        row2.add(new KeyboardButton("❓ Help"));
        keyboard.add(row2);

        return ReplyKeyboardMarkup.builder()
            .keyboard(keyboard)
            .resizeKeyboard(true)
            .oneTimeKeyboard(false)
            .build();
    }

    /**
     * Build cancel keyboard for operations
     */
    public InlineKeyboardMarkup buildCancelKeyboard() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createButton("❌ Cancel", "action:cancel"));
        rows.add(row);

        return InlineKeyboardMarkup.builder()
            .keyboard(rows)
            .build();
    }

    /**
     * Build pagination keyboard
     */
    public InlineKeyboardMarkup buildPaginationKeyboard(int currentPage, int totalPages, String baseCallback) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        if (currentPage > 0) {
            row.add(createButton("⬅️ Previous", baseCallback + ":page:" + (currentPage - 1)));
        }

        row.add(createButton(String.format("%d / %d", currentPage + 1, totalPages), "noop"));

        if (currentPage < totalPages - 1) {
            row.add(createButton("Next ➡️", baseCallback + ":page:" + (currentPage + 1)));
        }

        rows.add(row);

        return InlineKeyboardMarkup.builder()
            .keyboard(rows)
            .build();
    }

    /**
     * Helper method to create an inline button
     */
    private InlineKeyboardButton createButton(String text, String callbackData) {
        return InlineKeyboardButton.builder()
            .text(text)
            .callbackData(callbackData)
            .build();
    }

    /**
     * Helper method to create a URL button
     */
    private InlineKeyboardButton createUrlButton(String text, String url) {
        return InlineKeyboardButton.builder()
            .text(text)
            .url(url)
            .build();
    }
}
