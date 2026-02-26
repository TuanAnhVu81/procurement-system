package com.anhvt.epms.procurement.service;

import com.anhvt.epms.procurement.entity.Role;
import com.anhvt.epms.procurement.entity.User;
import com.anhvt.epms.procurement.enums.Status;
import com.anhvt.epms.procurement.service.impl.JwtServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtServiceImpl
 * Tests token generation, username extraction, and token validation
 * Uses ReflectionTestUtils to inject @Value fields without Spring context
 */
@DisplayName("JwtService Unit Tests")
class JwtServiceTest {

    private JwtServiceImpl jwtService;

    // A valid Base64-encoded 256-bit secret key for testing
    private static final String TEST_SECRET =
            Base64.getEncoder().encodeToString(
                    "test-secret-key-must-be-at-least-32-chars-long!".getBytes()
            );
    // 60 minutes in milliseconds
    private static final long TEST_EXPIRATION = 3_600_000L;

    private User testEmployee;
    private User testManager;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();
        // Inject @Value fields directly (no Spring ApplicationContext needed)
        ReflectionTestUtils.setField(jwtService, "signerKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", TEST_EXPIRATION);

        // Build a standard EMPLOYEE user for tests
        Role employeeRole = new Role();
        employeeRole.setName("ROLE_EMPLOYEE");

        testEmployee = User.builder()
                .username("john.doe")
                .email("john.doe@epms.com")
                .fullName("John Doe")
                .password("encoded_password")
                .status(Status.ACTIVE)
                .requirePasswordChange(false)
                .roles(Set.of(employeeRole))
                .build();
        testEmployee.setId(UUID.randomUUID());

        // Build a MANAGER user with requirePasswordChange=true (first-login scenario)
        Role managerRole = new Role();
        managerRole.setName("ROLE_MANAGER");

        testManager = User.builder()
                .username("jane.manager")
                .email("jane@epms.com")
                .fullName("Jane Manager")
                .password("encoded_password")
                .status(Status.ACTIVE)
                .requirePasswordChange(true) // first-login, password change required
                .roles(Set.of(managerRole))
                .build();
        testManager.setId(UUID.randomUUID());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GROUP 1: generateToken + extractUsername
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("generateToken() and extractUsername()")
    class GenerateAndExtractTests {

        @Test
        @DisplayName("Should generate a non-null, non-blank token")
        void shouldGenerateNonBlankToken() {
            // WHEN
            String token = jwtService.generateToken(testEmployee);

            // THEN
            assertThat(token).isNotBlank();
        }

        @Test
        @DisplayName("Should correctly extract username (subject) from generated token")
        void shouldExtractCorrectUsername() {
            // GIVEN
            String token = jwtService.generateToken(testEmployee);

            // WHEN
            String extractedUsername = jwtService.extractUsername(token);

            // THEN: Subject of JWT must equal user.getUsername()
            assertThat(extractedUsername).isEqualTo("john.doe");
        }

        @Test
        @DisplayName("Should embed requirePasswordChange=false in claims for normal user")
        void shouldEmbedRequirePasswordChange_false_forNormalUser() {
            // GIVEN: testEmployee has requirePasswordChange = false
            String token = jwtService.generateToken(testEmployee);

            // WHEN: Extract the custom claim
            Boolean requireChange = jwtService.extractClaim(token,
                    claims -> claims.get("requirePasswordChange", Boolean.class));

            // THEN
            assertThat(requireChange).isFalse();
        }

        @Test
        @DisplayName("Should embed requirePasswordChange=true in claims for first-time login user")
        void shouldEmbedRequirePasswordChange_true_forFirstLoginUser() {
            // GIVEN: testManager has requirePasswordChange = true
            String token = jwtService.generateToken(testManager);

            // WHEN
            Boolean requireChange = jwtService.extractClaim(token,
                    claims -> claims.get("requirePasswordChange", Boolean.class));

            // THEN: This claim enforces mandatory password change on frontend
            assertThat(requireChange).isTrue();
        }

        @Test
        @DisplayName("Should embed correct role in claims")
        void shouldEmbedRoleInClaims() {
            // GIVEN
            String token = jwtService.generateToken(testEmployee);

            // WHEN: Extract roles as raw object (JJWT returns List)
            Object roles = jwtService.extractClaim(token,
                    claims -> claims.get("roles"));

            // THEN
            assertThat(roles).isNotNull();
            assertThat(roles.toString()).contains("ROLE_EMPLOYEE");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GROUP 2: isTokenValid
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("isTokenValid()")
    class TokenValidationTests {

        @Test
        @DisplayName("Should return true for a freshly generated valid token")
        void shouldReturnTrue_forFreshToken() {
            // GIVEN
            String token = jwtService.generateToken(testEmployee);

            // WHEN + THEN
            assertThat(jwtService.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("Should return false for a tampered/malformed token")
        void shouldReturnFalse_forTamperedToken() {
            // GIVEN: A clearly invalid token string
            String invalidToken = "this.is.not.a.jwt";

            // WHEN + THEN
            assertThat(jwtService.isTokenValid(invalidToken)).isFalse();
        }

        @Test
        @DisplayName("Should return false for a token signed with a different key")
        void shouldReturnFalse_forTokenSignedWithDifferentKey() {
            // GIVEN: A separate JwtServiceImpl instance with a different secret
            JwtServiceImpl otherJwtService = new JwtServiceImpl();
            String differentSecret = Base64.getEncoder().encodeToString(
                    "completely-different-secret-key-32chars!".getBytes()
            );
            ReflectionTestUtils.setField(otherJwtService, "signerKey", differentSecret);
            ReflectionTestUtils.setField(otherJwtService, "expiration", TEST_EXPIRATION);

            // Token generated by other service (different key)
            String tokenFromOtherService = otherJwtService.generateToken(testEmployee);

            // WHEN: Validated by our service (different key) → should fail
            assertThat(jwtService.isTokenValid(tokenFromOtherService)).isFalse();
        }
    }
}
