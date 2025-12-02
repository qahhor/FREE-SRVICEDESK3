package io.greenwhite.servicedesk.channel.telegram;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TelegramKeyboardBuilder
 */
class TelegramKeyboardBuilderTest {

    private TelegramKeyboardBuilder keyboardBuilder;

    @BeforeEach
    void setUp() {
        keyboardBuilder = new TelegramKeyboardBuilder();
    }

    @Test
    @DisplayName("Should build main menu keyboard with correct buttons")
    void testBuildMainMenuKeyboard() {
        // When
        InlineKeyboardMarkup keyboard = keyboardBuilder.buildMainMenuKeyboard();

        // Then
        assertThat(keyboard).isNotNull();
        assertThat(keyboard.getKeyboard()).hasSize(3);

        // First row - Create Ticket
        List<InlineKeyboardButton> row1 = keyboard.getKeyboard().get(0);
        assertThat(row1).hasSize(1);
        assertThat(row1.get(0).getText()).contains("Create Ticket");
        assertThat(row1.get(0).getCallbackData()).isEqualTo("action:new_ticket");

        // Second row - Check Status and My Tickets
        List<InlineKeyboardButton> row2 = keyboard.getKeyboard().get(1);
        assertThat(row2).hasSize(2);
        assertThat(row2.get(0).getText()).contains("Check Status");
        assertThat(row2.get(1).getText()).contains("My Tickets");

        // Third row - Help
        List<InlineKeyboardButton> row3 = keyboard.getKeyboard().get(2);
        assertThat(row3).hasSize(1);
        assertThat(row3.get(0).getText()).contains("Help");
    }

    @Test
    @DisplayName("Should build priority selection keyboard")
    void testBuildPriorityKeyboard() {
        // When
        InlineKeyboardMarkup keyboard = keyboardBuilder.buildPriorityKeyboard();

        // Then
        assertThat(keyboard).isNotNull();
        assertThat(keyboard.getKeyboard()).hasSize(3);

        // First row - Critical and High
        List<InlineKeyboardButton> row1 = keyboard.getKeyboard().get(0);
        assertThat(row1).hasSize(2);
        assertThat(row1.get(0).getCallbackData()).isEqualTo("priority:CRITICAL");
        assertThat(row1.get(1).getCallbackData()).isEqualTo("priority:HIGH");

        // Second row - Medium and Low
        List<InlineKeyboardButton> row2 = keyboard.getKeyboard().get(1);
        assertThat(row2).hasSize(2);
        assertThat(row2.get(0).getCallbackData()).isEqualTo("priority:MEDIUM");
        assertThat(row2.get(1).getCallbackData()).isEqualTo("priority:LOW");

        // Third row - Cancel
        List<InlineKeyboardButton> row3 = keyboard.getKeyboard().get(2);
        assertThat(row3).hasSize(1);
        assertThat(row3.get(0).getCallbackData()).isEqualTo("action:cancel");
    }

    @Test
    @DisplayName("Should build confirmation keyboard")
    void testBuildConfirmationKeyboard() {
        // When
        InlineKeyboardMarkup keyboard = keyboardBuilder.buildConfirmationKeyboard(
            "action:confirm", "action:cancel");

        // Then
        assertThat(keyboard).isNotNull();
        assertThat(keyboard.getKeyboard()).hasSize(1);

        List<InlineKeyboardButton> row = keyboard.getKeyboard().get(0);
        assertThat(row).hasSize(2);
        assertThat(row.get(0).getText()).contains("Confirm");
        assertThat(row.get(0).getCallbackData()).isEqualTo("action:confirm");
        assertThat(row.get(1).getText()).contains("Cancel");
        assertThat(row.get(1).getCallbackData()).isEqualTo("action:cancel");
    }

    @Test
    @DisplayName("Should build ticket status filter keyboard")
    void testBuildTicketStatusFilterKeyboard() {
        // When
        InlineKeyboardMarkup keyboard = keyboardBuilder.buildTicketStatusFilterKeyboard();

        // Then
        assertThat(keyboard).isNotNull();
        assertThat(keyboard.getKeyboard()).hasSize(3);

        // Verify status options
        List<InlineKeyboardButton> row1 = keyboard.getKeyboard().get(0);
        assertThat(row1.get(0).getCallbackData()).isEqualTo("filter:status:NEW");
        assertThat(row1.get(1).getCallbackData()).isEqualTo("filter:status:OPEN");

        List<InlineKeyboardButton> row2 = keyboard.getKeyboard().get(1);
        assertThat(row2.get(0).getCallbackData()).isEqualTo("filter:status:PENDING");
        assertThat(row2.get(1).getCallbackData()).isEqualTo("filter:status:RESOLVED");

        List<InlineKeyboardButton> row3 = keyboard.getKeyboard().get(2);
        assertThat(row3.get(0).getCallbackData()).isEqualTo("filter:status:ALL");
    }

    @Test
    @DisplayName("Should build ticket actions keyboard")
    void testBuildTicketActionsKeyboard() {
        // Given
        String ticketId = "DESK-123";

        // When
        InlineKeyboardMarkup keyboard = keyboardBuilder.buildTicketActionsKeyboard(ticketId);

        // Then
        assertThat(keyboard).isNotNull();
        assertThat(keyboard.getKeyboard()).hasSize(2);

        // First row - Add Comment and Add File
        List<InlineKeyboardButton> row1 = keyboard.getKeyboard().get(0);
        assertThat(row1).hasSize(2);
        assertThat(row1.get(0).getCallbackData()).isEqualTo("ticket:DESK-123:comment");
        assertThat(row1.get(1).getCallbackData()).isEqualTo("ticket:DESK-123:file");

        // Second row - Refresh and Back
        List<InlineKeyboardButton> row2 = keyboard.getKeyboard().get(1);
        assertThat(row2).hasSize(2);
        assertThat(row2.get(0).getCallbackData()).isEqualTo("ticket:DESK-123:refresh");
        assertThat(row2.get(1).getCallbackData()).isEqualTo("action:my_tickets");
    }

    @Test
    @DisplayName("Should build reply keyboard")
    void testBuildReplyKeyboard() {
        // When
        ReplyKeyboardMarkup keyboard = keyboardBuilder.buildReplyKeyboard();

        // Then
        assertThat(keyboard).isNotNull();
        assertThat(keyboard.getKeyboard()).hasSize(2);
        assertThat(keyboard.getResizeKeyboard()).isTrue();
        assertThat(keyboard.getOneTimeKeyboard()).isFalse();
    }

    @Test
    @DisplayName("Should build cancel keyboard")
    void testBuildCancelKeyboard() {
        // When
        InlineKeyboardMarkup keyboard = keyboardBuilder.buildCancelKeyboard();

        // Then
        assertThat(keyboard).isNotNull();
        assertThat(keyboard.getKeyboard()).hasSize(1);

        List<InlineKeyboardButton> row = keyboard.getKeyboard().get(0);
        assertThat(row).hasSize(1);
        assertThat(row.get(0).getText()).contains("Cancel");
        assertThat(row.get(0).getCallbackData()).isEqualTo("action:cancel");
    }

    @Test
    @DisplayName("Should build pagination keyboard for first page")
    void testBuildPaginationKeyboardFirstPage() {
        // When
        InlineKeyboardMarkup keyboard = keyboardBuilder.buildPaginationKeyboard(0, 5, "tickets");

        // Then
        assertThat(keyboard).isNotNull();
        assertThat(keyboard.getKeyboard()).hasSize(1);

        List<InlineKeyboardButton> row = keyboard.getKeyboard().get(0);
        assertThat(row).hasSize(2); // Page info and Next button
        assertThat(row.get(0).getText()).isEqualTo("1 / 5");
        assertThat(row.get(1).getText()).contains("Next");
        assertThat(row.get(1).getCallbackData()).isEqualTo("tickets:page:1");
    }

    @Test
    @DisplayName("Should build pagination keyboard for middle page")
    void testBuildPaginationKeyboardMiddlePage() {
        // When
        InlineKeyboardMarkup keyboard = keyboardBuilder.buildPaginationKeyboard(2, 5, "tickets");

        // Then
        assertThat(keyboard).isNotNull();
        assertThat(keyboard.getKeyboard()).hasSize(1);

        List<InlineKeyboardButton> row = keyboard.getKeyboard().get(0);
        assertThat(row).hasSize(3); // Previous, Page info, Next
        assertThat(row.get(0).getText()).contains("Previous");
        assertThat(row.get(0).getCallbackData()).isEqualTo("tickets:page:1");
        assertThat(row.get(1).getText()).isEqualTo("3 / 5");
        assertThat(row.get(2).getText()).contains("Next");
        assertThat(row.get(2).getCallbackData()).isEqualTo("tickets:page:3");
    }

    @Test
    @DisplayName("Should build pagination keyboard for last page")
    void testBuildPaginationKeyboardLastPage() {
        // When
        InlineKeyboardMarkup keyboard = keyboardBuilder.buildPaginationKeyboard(4, 5, "tickets");

        // Then
        assertThat(keyboard).isNotNull();
        assertThat(keyboard.getKeyboard()).hasSize(1);

        List<InlineKeyboardButton> row = keyboard.getKeyboard().get(0);
        assertThat(row).hasSize(2); // Previous and Page info
        assertThat(row.get(0).getText()).contains("Previous");
        assertThat(row.get(0).getCallbackData()).isEqualTo("tickets:page:3");
        assertThat(row.get(1).getText()).isEqualTo("5 / 5");
    }
}
