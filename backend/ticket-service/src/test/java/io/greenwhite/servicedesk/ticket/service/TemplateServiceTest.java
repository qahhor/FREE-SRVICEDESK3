package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.common.enums.NotificationChannel;
import io.greenwhite.servicedesk.common.enums.NotificationEventType;
import io.greenwhite.servicedesk.ticket.model.NotificationTemplate;
import io.greenwhite.servicedesk.ticket.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TemplateService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TemplateService Tests")
class TemplateServiceTest {

    @Mock
    private NotificationTemplateRepository templateRepository;

    @InjectMocks
    private TemplateService templateService;

    private NotificationTemplate testTemplate;

    @BeforeEach
    void setUp() {
        testTemplate = NotificationTemplate.builder()
                .name("ticket-created-inapp")
                .eventType(NotificationEventType.TICKET_CREATED)
                .channel(NotificationChannel.IN_APP)
                .subject("New Ticket: {{ticketNumber}}")
                .bodyTemplate("A new ticket {{ticketNumber}} has been created: {{ticketSubject}}")
                .isActive(true)
                .language("en")
                .build();
        testTemplate.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should render template with variables")
    void shouldRenderTemplateWithVariables() {
        // Given
        String template = "Hello {{userName}}, ticket {{ticketNumber}} is {{status}}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", "John");
        variables.put("ticketNumber", "DESK-123");
        variables.put("status", "open");

        // When
        String result = templateService.renderTemplate(template, variables);

        // Then
        assertThat(result).isEqualTo("Hello John, ticket DESK-123 is open");
    }

    @Test
    @DisplayName("Should handle missing variables gracefully")
    void shouldHandleMissingVariablesGracefully() {
        // Given
        String template = "Hello {{userName}}, ticket {{ticketNumber}} is {{status}}";
        Map<String, Object> variables = new HashMap<>();
        variables.put("userName", "John");
        // ticketNumber and status are missing

        // When
        String result = templateService.renderTemplate(template, variables);

        // Then
        assertThat(result).isEqualTo("Hello John, ticket  is ");
    }

    @Test
    @DisplayName("Should get template for event type and channel")
    void shouldGetTemplateForEventTypeAndChannel() {
        // Given
        when(templateRepository.findByEventTypeAndChannelAndLanguageAndIsActiveTrue(
                NotificationEventType.TICKET_CREATED, NotificationChannel.IN_APP, "en"))
                .thenReturn(Optional.of(testTemplate));

        // When
        Optional<NotificationTemplate> result = templateService.getTemplate(
                NotificationEventType.TICKET_CREATED, NotificationChannel.IN_APP, "en");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("ticket-created-inapp");
    }

    @Test
    @DisplayName("Should fallback to English when language template not found")
    void shouldFallbackToEnglishWhenLanguageNotFound() {
        // Given
        when(templateRepository.findByEventTypeAndChannelAndLanguageAndIsActiveTrue(
                NotificationEventType.TICKET_CREATED, NotificationChannel.IN_APP, "ru"))
                .thenReturn(Optional.empty());
        when(templateRepository.findByEventTypeAndChannelAndLanguageAndIsActiveTrue(
                NotificationEventType.TICKET_CREATED, NotificationChannel.IN_APP, "en"))
                .thenReturn(Optional.of(testTemplate));

        // When
        Optional<NotificationTemplate> result = templateService.getTemplate(
                NotificationEventType.TICKET_CREATED, NotificationChannel.IN_APP, "ru");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getLanguage()).isEqualTo("en");
    }

    @Test
    @DisplayName("Should render title from template")
    void shouldRenderTitleFromTemplate() {
        // Given
        when(templateRepository.findByEventTypeAndChannelAndLanguageAndIsActiveTrue(
                NotificationEventType.TICKET_CREATED, NotificationChannel.IN_APP, "en"))
                .thenReturn(Optional.of(testTemplate));

        Map<String, Object> variables = new HashMap<>();
        variables.put("ticketNumber", "DESK-123");

        // When
        String result = templateService.renderTitle(
                NotificationEventType.TICKET_CREATED, NotificationChannel.IN_APP, variables, "en");

        // Then
        assertThat(result).isEqualTo("New Ticket: DESK-123");
    }

    @Test
    @DisplayName("Should use default title when template not found")
    void shouldUseDefaultTitleWhenTemplateNotFound() {
        // Given
        when(templateRepository.findByEventTypeAndChannelAndLanguageAndIsActiveTrue(
                NotificationEventType.TICKET_ASSIGNED, NotificationChannel.IN_APP, "en"))
                .thenReturn(Optional.empty());

        Map<String, Object> variables = new HashMap<>();

        // When
        String result = templateService.renderTitle(
                NotificationEventType.TICKET_ASSIGNED, NotificationChannel.IN_APP, variables, "en");

        // Then
        assertThat(result).isEqualTo("Ticket Assigned");
    }

    @Test
    @DisplayName("Should render message from template")
    void shouldRenderMessageFromTemplate() {
        // Given
        when(templateRepository.findByEventTypeAndChannelAndLanguageAndIsActiveTrue(
                NotificationEventType.TICKET_CREATED, NotificationChannel.IN_APP, "en"))
                .thenReturn(Optional.of(testTemplate));

        Map<String, Object> variables = new HashMap<>();
        variables.put("ticketNumber", "DESK-123");
        variables.put("ticketSubject", "Help needed");

        // When
        String result = templateService.renderMessage(
                NotificationEventType.TICKET_CREATED, NotificationChannel.IN_APP, variables, "en");

        // Then
        assertThat(result).isEqualTo("A new ticket DESK-123 has been created: Help needed");
    }

    @Test
    @DisplayName("Should handle null template gracefully")
    void shouldHandleNullTemplateGracefully() {
        // Given
        String result = templateService.renderTemplate(null, Map.of());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle empty template gracefully")
    void shouldHandleEmptyTemplateGracefully() {
        // Given
        String result = templateService.renderTemplate("", Map.of());

        // Then
        assertThat(result).isEmpty();
    }
}
