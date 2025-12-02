package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.common.enums.UserRole;
import io.greenwhite.servicedesk.common.exception.ResourceNotFoundException;
import io.greenwhite.servicedesk.ticket.dto.NotificationPreferenceRequest;
import io.greenwhite.servicedesk.ticket.dto.NotificationPreferenceResponse;
import io.greenwhite.servicedesk.ticket.model.NotificationPreference;
import io.greenwhite.servicedesk.ticket.model.User;
import io.greenwhite.servicedesk.ticket.repository.NotificationPreferenceRepository;
import io.greenwhite.servicedesk.ticket.repository.UserRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationPreferenceService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationPreferenceService Tests")
class NotificationPreferenceServiceTest {

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationPreferenceService preferenceService;

    private UUID userId;
    private User testUser;
    private NotificationPreference testPreferences;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUser = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.AGENT)
                .build();
        testUser.setId(userId);

        testPreferences = NotificationPreference.builder()
                .user(testUser)
                .emailEnabled(true)
                .inAppEnabled(true)
                .pushEnabled(false)
                .emailSettings(new HashMap<>(Map.of(
                        "ticket_created", true,
                        "ticket_assigned", true
                )))
                .inAppSettings(new HashMap<>(Map.of(
                        "ticket_created", true,
                        "ticket_assigned", true
                )))
                .timezone("UTC")
                .build();
        testPreferences.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should get existing preferences")
    void shouldGetExistingPreferences() {
        // Given
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

        // When
        NotificationPreferenceResponse result = preferenceService.getPreferences(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getEmailEnabled()).isTrue();
        assertThat(result.getInAppEnabled()).isTrue();
    }

    @Test
    @DisplayName("Should create default preferences when not found")
    void shouldCreateDefaultPreferencesWhenNotFound() {
        // Given
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(preferenceRepository.save(any(NotificationPreference.class))).thenReturn(testPreferences);

        // When
        NotificationPreferenceResponse result = preferenceService.getPreferences(userId);

        // Then
        assertThat(result).isNotNull();
        verify(preferenceRepository).save(any(NotificationPreference.class));
    }

    @Test
    @DisplayName("Should update preferences")
    void shouldUpdatePreferences() {
        // Given
        NotificationPreferenceRequest request = NotificationPreferenceRequest.builder()
                .emailEnabled(false)
                .inAppEnabled(true)
                .pushEnabled(true)
                .timezone("America/New_York")
                .build();

        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));
        when(preferenceRepository.save(any(NotificationPreference.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        NotificationPreferenceResponse result = preferenceService.updatePreferences(userId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmailEnabled()).isFalse();
        assertThat(result.getInAppEnabled()).isTrue();
        assertThat(result.getPushEnabled()).isTrue();
        assertThat(result.getTimezone()).isEqualTo("America/New_York");
    }

    @Test
    @DisplayName("Should update specific email settings")
    void shouldUpdateSpecificEmailSettings() {
        // Given
        Map<String, Boolean> newEmailSettings = new HashMap<>();
        newEmailSettings.put("ticket_created", false);
        newEmailSettings.put("ticket_assigned", false);

        NotificationPreferenceRequest request = NotificationPreferenceRequest.builder()
                .emailSettings(newEmailSettings)
                .build();

        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));
        when(preferenceRepository.save(any(NotificationPreference.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        NotificationPreferenceResponse result = preferenceService.updatePreferences(userId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmailSettings()).containsEntry("ticket_created", false);
        assertThat(result.getEmailSettings()).containsEntry("ticket_assigned", false);
    }

    @Test
    @DisplayName("Should create default preferences for new user")
    void shouldCreateDefaultPreferencesForNewUser() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(preferenceRepository.save(any(NotificationPreference.class))).thenAnswer(inv -> {
            NotificationPreference pref = inv.getArgument(0);
            pref.setId(UUID.randomUUID());
            return pref;
        });

        // When
        NotificationPreference result = preferenceService.createDefaultPreferences(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmailEnabled()).isTrue();
        assertThat(result.getInAppEnabled()).isTrue();
        assertThat(result.getPushEnabled()).isFalse();
        assertThat(result.getTimezone()).isEqualTo("UTC");
        verify(preferenceRepository).save(any(NotificationPreference.class));
    }

    @Test
    @DisplayName("Should throw exception when creating preferences for non-existent user")
    void shouldThrowExceptionWhenCreatingPreferencesForNonExistentUser() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> preferenceService.createDefaultPreferences(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should handle partial update")
    void shouldHandlePartialUpdate() {
        // Given
        NotificationPreferenceRequest request = NotificationPreferenceRequest.builder()
                .emailEnabled(false)
                // Other fields are null, should not be updated
                .build();

        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));
        when(preferenceRepository.save(any(NotificationPreference.class))).thenAnswer(inv -> inv.getArgument(0));

        // When
        NotificationPreferenceResponse result = preferenceService.updatePreferences(userId, request);

        // Then
        assertThat(result.getEmailEnabled()).isFalse();
        // Other fields should remain unchanged
        assertThat(result.getInAppEnabled()).isTrue();
        assertThat(result.getTimezone()).isEqualTo("UTC");
    }
}
