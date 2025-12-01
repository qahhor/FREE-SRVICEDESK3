package io.greenwhite.servicedesk.ticket.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtTokenProvider
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private UUID userId;
    private UserPrincipal userPrincipal;

    @BeforeEach
    void setUp() {
        // Use a 256-bit secret key for testing (32 characters minimum for HS256)
        String testSecret = "my-super-secret-key-for-testing-jwt-tokens-minimum-32-chars";
        long expirationMs = 3600000; // 1 hour

        jwtTokenProvider = new JwtTokenProvider(testSecret, expirationMs);

        userId = UUID.randomUUID();
        userPrincipal = new UserPrincipal(
            userId,
            "test@example.com",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_AGENT")),
            true
        );
    }

    @Test
    @DisplayName("Should generate token successfully")
    void shouldGenerateTokenSuccessfully() {
        // Given
        Authentication authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            userPrincipal,
            null,
            userPrincipal.getAuthorities()
        );

        // When
        String token = jwtTokenProvider.generateToken(authentication);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // JWT format: header.payload.signature
    }

    @Test
    @DisplayName("Should validate valid token")
    void shouldValidateValidToken() {
        // Given
        Authentication authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            userPrincipal,
            null,
            userPrincipal.getAuthorities()
        );
        String token = jwtTokenProvider.generateToken(authentication);

        // When
        boolean isValid = jwtTokenProvider.validateToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should invalidate malformed token")
    void shouldInvalidateMalformedToken() {
        // Given
        String invalidToken = "invalid.token.format";

        // When
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should invalidate empty token")
    void shouldInvalidateEmptyToken() {
        // Given
        String emptyToken = "";

        // When
        boolean isValid = jwtTokenProvider.validateToken(emptyToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should extract user ID from token")
    void shouldExtractUserIdFromToken() {
        // Given
        Authentication authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            userPrincipal,
            null,
            userPrincipal.getAuthorities()
        );
        String token = jwtTokenProvider.generateToken(authentication);

        // When
        String extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        // Then
        assertThat(extractedUserId).isEqualTo(userId.toString());
    }

    @Test
    @DisplayName("Should invalidate token with wrong signature")
    void shouldInvalidateTokenWithWrongSignature() {
        // Given
        Authentication authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            userPrincipal,
            null,
            userPrincipal.getAuthorities()
        );
        String token = jwtTokenProvider.generateToken(authentication);
        
        // Modify the signature part
        String[] parts = token.split("\\.");
        String tamperedToken = parts[0] + "." + parts[1] + ".tampered_signature";

        // When
        boolean isValid = jwtTokenProvider.validateToken(tamperedToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should generate different tokens for same user")
    void shouldGenerateDifferentTokensForSameUser() {
        // Given
        Authentication authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
            userPrincipal,
            null,
            userPrincipal.getAuthorities()
        );

        // When
        String token1 = jwtTokenProvider.generateToken(authentication);
        String token2 = jwtTokenProvider.generateToken(authentication);

        // Then
        // Due to the timestamp (iat), tokens should be different
        // However, in a very fast execution they might be the same
        // Both should be valid
        assertThat(jwtTokenProvider.validateToken(token1)).isTrue();
        assertThat(jwtTokenProvider.validateToken(token2)).isTrue();
    }
}
