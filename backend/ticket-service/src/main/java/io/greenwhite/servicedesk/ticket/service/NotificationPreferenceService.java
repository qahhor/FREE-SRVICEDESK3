package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.common.exception.ResourceNotFoundException;
import io.greenwhite.servicedesk.ticket.dto.NotificationPreferenceRequest;
import io.greenwhite.servicedesk.ticket.dto.NotificationPreferenceResponse;
import io.greenwhite.servicedesk.ticket.model.NotificationPreference;
import io.greenwhite.servicedesk.ticket.model.User;
import io.greenwhite.servicedesk.ticket.repository.NotificationPreferenceRepository;
import io.greenwhite.servicedesk.ticket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for managing notification preferences
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    private static final Map<String, Boolean> DEFAULT_EMAIL_SETTINGS = Map.of(
            "ticket_created", true,
            "ticket_assigned", true,
            "ticket_commented", true,
            "ticket_resolved", true,
            "sla_warning", true,
            "sla_breach", true
    );

    private static final Map<String, Boolean> DEFAULT_IN_APP_SETTINGS = Map.of(
            "ticket_created", true,
            "ticket_assigned", true,
            "ticket_commented", true,
            "ticket_resolved", true,
            "sla_warning", true,
            "sla_breach", true
    );

    /**
     * Get notification preferences for a user
     */
    @Transactional(readOnly = true)
    public NotificationPreferenceResponse getPreferences(UUID userId) {
        NotificationPreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));
        return mapToResponse(preference);
    }

    /**
     * Update notification preferences
     */
    @Transactional
    public NotificationPreferenceResponse updatePreferences(UUID userId, NotificationPreferenceRequest request) {
        NotificationPreference preference = preferenceRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultPreferences(userId));

        // Update fields if provided
        if (request.getEmailEnabled() != null) {
            preference.setEmailEnabled(request.getEmailEnabled());
        }
        if (request.getInAppEnabled() != null) {
            preference.setInAppEnabled(request.getInAppEnabled());
        }
        if (request.getPushEnabled() != null) {
            preference.setPushEnabled(request.getPushEnabled());
        }
        if (request.getEmailSettings() != null) {
            preference.setEmailSettings(request.getEmailSettings());
        }
        if (request.getInAppSettings() != null) {
            preference.setInAppSettings(request.getInAppSettings());
        }
        if (request.getQuietHoursEnabled() != null) {
            preference.setQuietHoursEnabled(request.getQuietHoursEnabled());
        }
        if (request.getQuietHoursStart() != null) {
            preference.setQuietHoursStart(request.getQuietHoursStart());
        }
        if (request.getQuietHoursEnd() != null) {
            preference.setQuietHoursEnd(request.getQuietHoursEnd());
        }
        if (request.getTimezone() != null) {
            preference.setTimezone(request.getTimezone());
        }

        preference = preferenceRepository.save(preference);
        log.info("Notification preferences updated for user {}", userId);
        
        return mapToResponse(preference);
    }

    /**
     * Create default preferences for a user
     */
    @Transactional
    public NotificationPreference createDefaultPreferences(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        NotificationPreference preference = NotificationPreference.builder()
                .user(user)
                .emailEnabled(true)
                .inAppEnabled(true)
                .pushEnabled(false)
                .emailSettings(new HashMap<>(DEFAULT_EMAIL_SETTINGS))
                .inAppSettings(new HashMap<>(DEFAULT_IN_APP_SETTINGS))
                .quietHoursEnabled(false)
                .timezone("UTC")
                .build();

        preference = preferenceRepository.save(preference);
        log.info("Default notification preferences created for user {}", userId);
        
        return preference;
    }

    /**
     * Map entity to response DTO
     */
    private NotificationPreferenceResponse mapToResponse(NotificationPreference preference) {
        return NotificationPreferenceResponse.builder()
                .id(preference.getId())
                .userId(preference.getUser().getId())
                .emailEnabled(preference.getEmailEnabled())
                .inAppEnabled(preference.getInAppEnabled())
                .pushEnabled(preference.getPushEnabled())
                .emailSettings(preference.getEmailSettings())
                .inAppSettings(preference.getInAppSettings())
                .quietHoursEnabled(preference.getQuietHoursEnabled())
                .quietHoursStart(preference.getQuietHoursStart())
                .quietHoursEnd(preference.getQuietHoursEnd())
                .timezone(preference.getTimezone())
                .build();
    }
}
