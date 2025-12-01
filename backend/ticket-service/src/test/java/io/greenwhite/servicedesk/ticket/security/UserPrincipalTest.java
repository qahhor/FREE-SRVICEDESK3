package io.greenwhite.servicedesk.ticket.security;

import io.greenwhite.servicedesk.common.enums.UserRole;
import io.greenwhite.servicedesk.ticket.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for UserPrincipal
 */
@DisplayName("UserPrincipal Tests")
class UserPrincipalTest {

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
    @DisplayName("Should create UserPrincipal from User entity")
    void shouldCreateUserPrincipalFromUser() {
        // When
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        // Then
        assertThat(userPrincipal.getId()).isEqualTo(userId);
        assertThat(userPrincipal.getEmail()).isEqualTo("test@example.com");
        assertThat(userPrincipal.getPassword()).isEqualTo("encoded_password");
        assertThat(userPrincipal.getUsername()).isEqualTo("test@example.com");
        assertThat(userPrincipal.isEnabled()).isTrue();
        assertThat(userPrincipal.isAccountNonExpired()).isTrue();
        assertThat(userPrincipal.isAccountNonLocked()).isTrue();
        assertThat(userPrincipal.isCredentialsNonExpired()).isTrue();
        assertThat(userPrincipal.getAuthorities()).hasSize(1);
        assertThat(userPrincipal.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_AGENT");
    }

    @Test
    @DisplayName("Should return isEnabled false when user is inactive")
    void shouldReturnIsEnabledFalseWhenUserInactive() {
        // Given
        testUser.setActive(false);

        // When
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        // Then
        assertThat(userPrincipal.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("Should have correct role for ADMIN user")
    void shouldHaveCorrectRoleForAdminUser() {
        // Given
        testUser.setRole(UserRole.ADMIN);

        // When
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        // Then
        assertThat(userPrincipal.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Should have correct role for CUSTOMER user")
    void shouldHaveCorrectRoleForCustomerUser() {
        // Given
        testUser.setRole(UserRole.CUSTOMER);

        // When
        UserPrincipal userPrincipal = UserPrincipal.create(testUser);

        // Then
        assertThat(userPrincipal.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_CUSTOMER");
    }
}
