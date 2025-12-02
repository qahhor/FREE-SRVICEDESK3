package io.greenwhite.servicedesk.ticket.controller;

import io.greenwhite.servicedesk.common.dto.ApiResponse;
import io.greenwhite.servicedesk.ticket.dto.NotificationPreferenceRequest;
import io.greenwhite.servicedesk.ticket.dto.NotificationPreferenceResponse;
import io.greenwhite.servicedesk.ticket.dto.NotificationResponse;
import io.greenwhite.servicedesk.ticket.security.UserPrincipal;
import io.greenwhite.servicedesk.ticket.service.InAppNotificationService;
import io.greenwhite.servicedesk.ticket.service.NotificationPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Controller for notification operations
 */
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final InAppNotificationService inAppNotificationService;
    private final NotificationPreferenceService preferenceService;

    /**
     * Get user notifications with pagination
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal UserPrincipal currentUser,
            Pageable pageable) {
        Page<NotificationResponse> notifications = inAppNotificationService.getUserNotifications(
                currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * Get unread notifications
     */
    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getUnreadNotifications(
            @AuthenticationPrincipal UserPrincipal currentUser,
            Pageable pageable) {
        Page<NotificationResponse> notifications = inAppNotificationService.getUnreadNotifications(
                currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    /**
     * Get unread notification count
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        long count = inAppNotificationService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", count)));
    }

    /**
     * Mark notification as read
     */
    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        NotificationResponse notification = inAppNotificationService.markAsRead(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", notification));
    }

    /**
     * Mark all notifications as read
     */
    @PatchMapping("/read-all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        int count = inAppNotificationService.markAllAsRead(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", Map.of("updated", count)));
    }

    /**
     * Delete a notification
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(
            @PathVariable("id") UUID id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        inAppNotificationService.deleteNotification(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Notification deleted", null));
    }

    /**
     * Get notification preferences
     */
    @GetMapping("/preferences")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> getPreferences(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        NotificationPreferenceResponse preferences = preferenceService.getPreferences(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(preferences));
    }

    /**
     * Update notification preferences
     */
    @PutMapping("/preferences")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'AGENT', 'CUSTOMER')")
    public ResponseEntity<ApiResponse<NotificationPreferenceResponse>> updatePreferences(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody NotificationPreferenceRequest request) {
        NotificationPreferenceResponse preferences = preferenceService.updatePreferences(
                currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Preferences updated", preferences));
    }
}
