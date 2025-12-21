package com.mediaportal.cms.security;

import com.mediaportal.cms.model.Role;
import com.mediaportal.cms.model.User;
import com.mediaportal.cms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomUserDetailsService.
 */
@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRole(Role.USER);
        testUser.setIsActive(true);
    }

    @Test
    @DisplayName("Should load user by username successfully")
    void loadUserByUsername_ShouldReturnUserDetails() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("testuser");

        // Then
        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertTrue(userDetails.isEnabled());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void loadUserByUsername_WhenUserNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("nonexistent"));
        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    @DisplayName("Should get user entity by username")
    void getUserByUsername_ShouldReturnUser() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // When
        User user = userDetailsService.getUserByUsername("testuser");

        // Then
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals(1L, user.getId());
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent user")
    void getUserByUsername_WhenUserNotFound_ShouldThrowException() {
        // Given
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When/Then
        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.getUserByUsername("nonexistent"));
    }

    @Test
    @DisplayName("Should handle ADMIN role correctly")
    void loadUserByUsername_WithAdminRole_ShouldHaveAdminAuthority() {
        // Given
        testUser.setRole(Role.ADMIN);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        // Then
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("Should handle inactive user")
    void loadUserByUsername_WhenUserInactive_ShouldReturnDisabledUser() {
        // Given
        testUser.setIsActive(false);
        when(userRepository.findByUsername("inactive")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("inactive");

        // Then
        assertFalse(userDetails.isEnabled());
    }

    @Test
    @DisplayName("Should handle EDITOR role correctly")
    void loadUserByUsername_WithEditorRole_ShouldHaveEditorAuthority() {
        // Given
        testUser.setRole(Role.EDITOR);
        when(userRepository.findByUsername("editor")).thenReturn(Optional.of(testUser));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername("editor");

        // Then
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EDITOR")));
    }
}
