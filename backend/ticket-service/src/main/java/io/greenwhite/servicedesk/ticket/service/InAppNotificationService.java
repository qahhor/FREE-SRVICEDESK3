package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.common.enums.NotificationCategory;
import io.greenwhite.servicedesk.common.enums.NotificationChannel;
import io.greenwhite.servicedesk.common.enums.NotificationEventType;
import io.greenwhite.servicedesk.common.exception.ResourceNotFoundException;
import io.greenwhite.servicedesk.ticket.dto.NotificationResponse;
import io.greenwhite.servicedesk.ticket.model.Notification;
import io.greenwhite.servicedesk.ticket.model.NotificationPreference;
import io.greenwhite.servicedesk.ticket.model.User;
import io.greenwhite.servicedesk.ticket.repository.NotificationPreferenceRepository;
import io.greenwhite.servicedesk.ticket.repository.NotificationRepository;
import io.greenwhite.servicedesk.ticket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for in-app notifications with WebSocket support
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InAppNotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;
    private final TemplateService templateService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Send in-app notification to a user
     */
    @Transactional
    public NotificationResponse sendNotification(UUID userId, NotificationEventType eventType,
                                                 NotificationCategory category, Map<String, Object> data,
                                                 String referenceType, UUID referenceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Check user preferences
        Optional<NotificationPreference> preferences = preferenceRepository.findByUserId(userId);
        if (preferences.isPresent()) {
            NotificationPreference pref = preferences.get();
            if (!pref.isInAppNotificationEnabled(eventType.name())) {
                log.debug("In-app notification disabled for user {} and event {}", userId, eventType);
                return null;
            }
            if (pref.isInQuietHours()) {
                log.debug("User {} is in quiet hours, skipping in-app notification", userId);
                return null;
            }
        }

        // Get user language for template
        String language = user.getLanguage() != null ? user.getLanguage() : "en";

        // Render title and message from template
        String title = templateService.renderTitle(eventType, NotificationChannel.IN_APP, data, language);
        String message = templateService.renderMessage(eventType, NotificationChannel.IN_APP, data, language);

        // Create notification entity
        Notification notification = Notification.builder()
                .user(user)
                .type(NotificationChannel.IN_APP)
                .category(category)
                .title(title)
                .message(message)
                .data(data)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .sentAt(LocalDateTime.now())
                .build();

        notification = notificationRepository.save(notification);
        NotificationResponse response = mapToResponse(notification);

        // Send via WebSocket
        sendWebSocketNotification(userId, response);

        log.info("In-app notification sent to user {}: {}", userId, title);
        return response;
    }

    /**
     * Send notification via WebSocket
     */
    private void sendWebSocketNotification(UUID userId, NotificationResponse notification) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications",
                    notification
            );
            log.debug("WebSocket notification sent to user {}", userId);
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification to user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Broadcast unread count update
     */
    public void broadcastUnreadCount(UUID userId) {
        long unreadCount = notificationRepository.countByUserIdAndReadAtIsNull(userId);
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/notifications/count",
                    unreadCount
            );
        } catch (Exception e) {
            log.error("Failed to broadcast unread count to user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Get all notifications for a user
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get unread notifications for a user
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUnreadNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdAndReadAtIsNullOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get unread notification count
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadAtIsNull(userId);
    }

    /**
     * Mark notification as read
     */
    @Transactional
    public NotificationResponse markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        // Verify ownership
        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification", "id", notificationId);
        }

        notification.markAsRead();
        notification = notificationRepository.save(notification);

        // Broadcast updated count
        broadcastUnreadCount(userId);

        return mapToResponse(notification);
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public int markAllAsRead(UUID userId) {
        int updated = notificationRepository.markAllAsReadForUser(userId);
        
        // Broadcast updated count
        broadcastUnreadCount(userId);
        
        return updated;
    }

    /**
     * Delete a notification
     */
    @Transactional
    public void deleteNotification(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));

        // Verify ownership
        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification", "id", notificationId);
        }

        notificationRepository.delete(notification);
        
        // Broadcast updated count
        broadcastUnreadCount(userId);
    }

    /**
     * Map entity to response DTO
     */
    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .type(notification.getType())
                .category(notification.getCategory())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .data(notification.getData())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .readAt(notification.getReadAt())
                .sentAt(notification.getSentAt())
                .createdAt(notification.getCreatedAt())
                .read(notification.isRead())
                .build();
    }
}
