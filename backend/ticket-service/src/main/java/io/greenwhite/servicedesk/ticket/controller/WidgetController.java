package io.greenwhite.servicedesk.ticket.controller;

import io.greenwhite.servicedesk.common.dto.ApiResponse;
import io.greenwhite.servicedesk.ticket.dto.*;
import io.greenwhite.servicedesk.ticket.service.WidgetSessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for widget API endpoints
 */
@RestController
@RequestMapping("/widget")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class WidgetController {

    private final WidgetSessionService widgetSessionService;

    private static final String SESSION_HEADER = "X-Widget-Session";

    /**
     * Start a new widget session
     */
    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<WidgetSessionResponse>> startSession(
            @Valid @RequestBody WidgetSessionRequest request,
            HttpServletRequest httpRequest) {
        
        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        WidgetSessionResponse session = widgetSessionService.startSession(request, ipAddress, userAgent);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Session started successfully", session));
    }

    /**
     * Get session information
     */
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<WidgetSessionResponse>> getSession(
            @PathVariable UUID sessionId,
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken) {
        
        WidgetSessionResponse session = widgetSessionService.getSession(sessionId);
        return ResponseEntity.ok(ApiResponse.success(session));
    }

    /**
     * Send a message in a session
     */
    @PostMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ApiResponse<WidgetMessageResponse>> sendMessage(
            @PathVariable UUID sessionId,
            @Valid @RequestBody WidgetMessageRequest request,
            @RequestHeader(SESSION_HEADER) String sessionToken) {
        
        WidgetMessageResponse message = widgetSessionService.sendVisitorMessage(sessionId, request, sessionToken);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message sent successfully", message));
    }

    /**
     * Get all messages for a session
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ApiResponse<List<WidgetMessageResponse>>> getMessages(
            @PathVariable UUID sessionId,
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionToken) {
        
        List<WidgetMessageResponse> messages = widgetSessionService.getMessages(sessionId);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    /**
     * Close a session
     */
    @PostMapping("/sessions/{sessionId}/close")
    public ResponseEntity<ApiResponse<Void>> closeSession(
            @PathVariable UUID sessionId,
            @RequestHeader(SESSION_HEADER) String sessionToken) {
        
        widgetSessionService.closeSession(sessionId, sessionToken);
        return ResponseEntity.ok(ApiResponse.success("Session closed successfully", null));
    }

    /**
     * Get widget configuration
     */
    @GetMapping("/config")
    public ResponseEntity<ApiResponse<WidgetConfigResponse>> getConfig(
            @RequestParam String projectKey) {
        
        WidgetConfigResponse config = widgetSessionService.getConfig(projectKey);
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    /**
     * Get client IP address from request
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
