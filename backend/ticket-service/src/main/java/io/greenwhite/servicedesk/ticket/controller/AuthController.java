package io.greenwhite.servicedesk.ticket.controller;

import io.greenwhite.servicedesk.common.dto.ApiResponse;
import io.greenwhite.servicedesk.ticket.dto.LoginRequest;
import io.greenwhite.servicedesk.ticket.dto.LoginResponse;
import io.greenwhite.servicedesk.ticket.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<String>> getCurrentUser() {
        return ResponseEntity.ok(ApiResponse.success("Current user info", "User authenticated"));
    }
}
