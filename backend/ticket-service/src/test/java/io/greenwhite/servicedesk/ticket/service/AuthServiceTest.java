package io.greenwhite.servicedesk.ticket.service;

import io.greenwhite.servicedesk.common.enums.UserRole;
import io.greenwhite.servicedesk.ticket.dto.LoginRequest;
import io.greenwhite.servicedesk.ticket.dto.LoginResponse;
import io.greenwhite.servicedesk.ticket.model.User;
import io.greenwhite.servicedesk.ticket.repository.UserRepository;
import io.greenwhite.servicedesk.ticket.security.JwtTokenProvider;
import io.greenwhite.servicedesk.ticket.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        testUser = User.builder()
                .email("test@example.com")
                .password("encoded_password")
                .firstName("Test")
                .lastName("User")
                .role(UserRole.AGENT)
                .active(true)
                .build();
        testUser.setId(userId);
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        UserPrincipal userPrincipal = new UserPrincipal(
            userId, 
            testUser.getEmail(), 
            testUser.getPassword(),
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_AGENT")),
            true
        );
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal, 
            null, 
            userPrincipal.getAuthorities()
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt_token");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        // When
        LoginResponse response = authService.login(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("jwt_token");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
        assertThat(response.getUser().getRole()).isEqualTo(UserRole.AGENT);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateToken(authentication);
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    @DisplayName("Should throw exception when user not found after authentication")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setEmail("unknown@example.com");
        request.setPassword("password");

        UserPrincipal userPrincipal = new UserPrincipal(
            userId, 
            "unknown@example.com", 
            "encoded_password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_AGENT")),
            true
        );
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal, 
            null, 
            userPrincipal.getAuthorities()
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwt_token");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("unknown@example.com");
    }
}
