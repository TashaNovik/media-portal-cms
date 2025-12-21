package com.mediaportal.cms.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for JwtTokenProvider.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "jwt.secret=TestSecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong12345",
        "jwt.expiration=3600000"
})
class JwtTokenProviderTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = User.builder()
                .username("testuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    @DisplayName("Should generate valid JWT token")
    void generateToken_ShouldReturnValidToken() {
        // When
        String token = jwtTokenProvider.generateToken(userDetails);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    @DisplayName("Should extract username from token")
    void extractUsername_ShouldReturnCorrectUsername() {
        // Given
        String token = jwtTokenProvider.generateToken(userDetails);

        // When
        String username = jwtTokenProvider.extractUsername(token);

        // Then
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Should extract expiration date from token")
    void extractExpiration_ShouldReturnFutureDate() {
        // Given
        String token = jwtTokenProvider.generateToken(userDetails);

        // When
        Date expiration = jwtTokenProvider.extractExpiration(token);

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    @DisplayName("Should validate token with correct user")
    void validateToken_WithCorrectUser_ShouldReturnTrue() {
        // Given
        String token = jwtTokenProvider.generateToken(userDetails);

        // When
        Boolean isValid = jwtTokenProvider.validateToken(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Should invalidate token with different user")
    void validateToken_WithDifferentUser_ShouldReturnFalse() {
        // Given
        String token = jwtTokenProvider.generateToken(userDetails);
        UserDetails differentUser = User.builder()
                .username("differentuser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // When
        Boolean isValid = jwtTokenProvider.validateToken(token, differentUser);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Should generate different tokens for different users")
    void generateToken_ForDifferentUsers_ShouldBeDifferent() {
        // Given
        UserDetails anotherUser = User.builder()
                .username("anotheruser")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // When
        String token1 = jwtTokenProvider.generateToken(userDetails);
        String token2 = jwtTokenProvider.generateToken(anotherUser);

        // Then
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Should throw exception for invalid token")
    void extractUsername_WithInvalidToken_ShouldThrowException() {
        // Given
        String invalidToken = "invalid.token.here";

        // When/Then
        assertThrows(Exception.class, () -> jwtTokenProvider.extractUsername(invalidToken));
    }

    @Test
    @DisplayName("Should throw exception for malformed token")
    void validateToken_WithMalformedToken_ShouldThrowException() {
        // Given
        String malformedToken = "not-a-jwt";

        // When/Then
        assertThrows(Exception.class, () -> jwtTokenProvider.extractUsername(malformedToken));
    }
}
