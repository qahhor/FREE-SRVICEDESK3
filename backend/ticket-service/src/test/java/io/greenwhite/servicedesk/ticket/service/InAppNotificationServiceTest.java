package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.common.enums.NotificationCategory;
import io.greenwhite.servicedesk.common.enums.NotificationChannel;
import io.greenwhite.servicedesk.common.enums.NotificationEventType;
import io.greenwhite.servicedesk.common.enums.UserRole;
import io.greenwhite.servicedesk.common.exception.ResourceNotFoundException;
import io.greenwhite.servicedesk.ticket.dto.NotificationResponse;
import io.greenwhite.servicedesk.ticket.model.Notification;
import io.greenwhite.servicedesk.ticket.model.NotificationPreference;
import io.greenwhite.servicedesk.ticket.model.User;
import io.greenwhite.servicedesk.ticket.repository.NotificationPreferenceRepository;
import io.greenwhite.servicedesk.ticket.repository.NotificationRepository;
import io.greenwhite.servicedesk.ticket.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InAppNotificationService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InAppNotificationService Tests")
class InAppNotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationPreferenceRepository preferenceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TemplateService templateService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private InAppNotificationService inAppNotificationService;

    private UUID userId;
    private User testUser;
    private Notification testNotification;
    private NotificationPreference testPreferences;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();

        testUser = User.builder()
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.AGENT)
                .language("en")
                .build();
        testUser.setId(userId);

        testNotification = Notification.builder()
                .user(testUser)
                .type(NotificationChannel.IN_APP)
                .category(NotificationCategory.TICKET)
                .title("Test Notification")
                .message("This is a test notification")
                .referenceType("TICKET")
                .referenceId(UUID.randomUUID())
                .sentAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        testNotification.setId(UUID.randomUUID());

        testPreferences = NotificationPreference.builder()
                .user(testUser)
                .emailEnabled(true)
                .inAppEnabled(true)
                .pushEnabled(false)
                .inAppSettings(new HashMap<>(Map.of(
                        "ticket_created", true,
                        "ticket_assigned", true
                )))
                .build();
        testPreferences.setId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should send notification successfully")
    void shouldSendNotificationSuccessfully() {
        // Given
        Map<String, Object> data = new HashMap<>();
        data.put("ticketNumber", "DESK-123");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));
        when(templateService.renderTitle(any(), eq(NotificationChannel.IN_APP), any(), eq("en")))
                .thenReturn("Test Title");
        when(templateService.renderMessage(any(), eq(NotificationChannel.IN_APP), any(), eq("en")))
                .thenReturn("Test Message");
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // When
        NotificationResponse result = inAppNotificationService.sendNotification(
                userId,
                NotificationEventType.TICKET_CREATED,
                NotificationCategory.TICKET,
                data,
                "TICKET",
                UUID.randomUUID()
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(NotificationChannel.IN_APP);
        assertThat(result.getCategory()).isEqualTo(NotificationCategory.TICKET);
        verify(notificationRepository).save(any(Notification.class));
        verify(messagingTemplate).convertAndSendToUser(eq(userId.toString()), eq("/queue/notifications"), any());
    }

    @Test
    @DisplayName("Should skip notification when in-app disabled")
    void shouldSkipNotificationWhenInAppDisabled() {
        // Given
        testPreferences.setInAppEnabled(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(preferenceRepository.findByUserId(userId)).thenReturn(Optional.of(testPreferences));

        // When
        NotificationResponse result = inAppNotificationService.sendNotification(
                userId,
                NotificationEventType.TICKET_CREATED,
                NotificationCategory.TICKET,
                new HashMap<>(),
                "TICKET",
                UUID.randomUUID()
        );

        // Then
        assertThat(result).isNull();
        verify(notificationRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSendToUser(any(), any(), any());
    }

    @Test
    @DisplayName("Should get user notifications")
    void shouldGetUserNotifications() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> notificationPage = new PageImpl<>(List.of(testNotification));
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable))
                .thenReturn(notificationPage);

        // When
        Page<NotificationResponse> result = inAppNotificationService.getUserNotifications(userId, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Test Notification");
    }

    @Test
    @DisplayName("Should get unread count")
    void shouldGetUnreadCount() {
        // Given
        when(notificationRepository.countByUserIdAndReadAtIsNull(userId)).thenReturn(5L);

        // When
        long result = inAppNotificationService.getUnreadCount(userId);

        // Then
        assertThat(result).isEqualTo(5L);
    }

    @Test
    @DisplayName("Should mark notification as read")
    void shouldMarkNotificationAsRead() {
        // Given
        UUID notificationId = testNotification.getId();
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(notificationRepository.countByUserIdAndReadAtIsNull(userId)).thenReturn(4L);

        // When
        NotificationResponse result = inAppNotificationService.markAsRead(notificationId, userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(testNotification.getReadAt()).isNotNull();
        verify(notificationRepository).save(testNotification);
    }

    @Test
    @DisplayName("Should throw exception when marking non-existent notification as read")
    void shouldThrowExceptionWhenMarkingNonExistentNotification() {
        // Given
        UUID notificationId = UUID.randomUUID();
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inAppNotificationService.markAsRead(notificationId, userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw exception when marking another user's notification")
    void shouldThrowExceptionWhenMarkingAnotherUsersNotification() {
        // Given
        UUID notificationId = testNotification.getId();
        UUID otherUserId = UUID.randomUUID();
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(testNotification));

        // When & Then
        assertThatThrownBy(() -> inAppNotificationService.markAsRead(notificationId, otherUserId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should mark all notifications as read")
    void shouldMarkAllNotificationsAsRead() {
        // Given
        when(notificationRepository.markAllAsReadForUser(userId)).thenReturn(5);
        when(notificationRepository.countByUserIdAndReadAtIsNull(userId)).thenReturn(0L);

        // When
        int result = inAppNotificationService.markAllAsRead(userId);

        // Then
        assertThat(result).isEqualTo(5);
        verify(notificationRepository).markAllAsReadForUser(userId);
    }

    @Test
    @DisplayName("Should delete notification")
    void shouldDeleteNotification() {
        // Given
        UUID notificationId = testNotification.getId();
        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.countByUserIdAndReadAtIsNull(userId)).thenReturn(4L);

        // When
        inAppNotificationService.deleteNotification(notificationId, userId);

        // Then
        verify(notificationRepository).delete(testNotification);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> inAppNotificationService.sendNotification(
                userId,
                NotificationEventType.TICKET_CREATED,
                NotificationCategory.TICKET,
                new HashMap<>(),
                "TICKET",
                UUID.randomUUID()
        )).isInstanceOf(ResourceNotFoundException.class);
    }
}
