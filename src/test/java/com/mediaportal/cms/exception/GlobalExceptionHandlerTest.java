package com.mediaportal.cms.exception;

import com.mediaportal.cms.dto.common.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler exceptionHandler;

    @Test
    @DisplayName("Should handle ResourceNotFoundException")
    void handleResourceNotFound_ShouldReturn404() {
        // Given
        ResourceNotFoundException ex = new ResourceNotFoundException("Article not found");

        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleResourceNotFound(ex);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().getError().getCode());
    }

    @Test
    @DisplayName("Should handle EntityNotFoundException")
    void handleEntityNotFound_ShouldReturn404() {
        // Given
        EntityNotFoundException ex = new EntityNotFoundException("Entity not found");

        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleEntityNotFound(ex);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ENTITY_NOT_FOUND", response.getBody().getError().getCode());
    }

    @Test
    @DisplayName("Should handle BusinessException")
    void handleBusinessException_ShouldReturn400() {
        // Given
        BusinessException ex = new BusinessException("CUSTOM_ERROR", "Business error");

        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleBusinessException(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("CUSTOM_ERROR", response.getBody().getError().getCode());
    }

    @Test
    @DisplayName("Should handle BusinessException without error code")
    void handleBusinessException_WithoutErrorCode_ShouldUseDefault() {
        // Given
        BusinessException ex = new BusinessException("Business error");

        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleBusinessException(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("BUSINESS_ERROR", response.getBody().getError().getCode());
    }

    @Test
    @DisplayName("Should handle UnauthorizedException")
    void handleUnauthorized_ShouldReturn403() {
        // Given
        UnauthorizedException ex = new UnauthorizedException("Not authorized");

        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleUnauthorized(ex);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("UNAUTHORIZED", response.getBody().getError().getCode());
    }

    @Test
    @DisplayName("Should handle AccessDeniedException")
    void handleAccessDenied_ShouldReturn403() {
        // Given
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleAccessDenied(ex);

        // Then
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("ACCESS_DENIED", response.getBody().getError().getCode());
    }

    @Test
    @DisplayName("Should handle BadCredentialsException")
    void handleBadCredentials_ShouldReturn401() {
        // Given
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleBadCredentials(ex);

        // Then
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("BAD_CREDENTIALS", response.getBody().getError().getCode());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException")
    void handleIllegalArgument_ShouldReturn400() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument");

        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleIllegalArgument(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("INVALID_ARGUMENT", response.getBody().getError().getCode());
    }

    @Test
    @DisplayName("Should handle RuntimeException with 'already exists' message")
    void handleRuntimeException_WithAlreadyExists_ShouldReturn400() {
        // Given
        RuntimeException ex = new RuntimeException("Username already exists");

        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleRuntimeException(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("BAD_REQUEST", response.getBody().getError().getCode());
    }

    @Test
    @DisplayName("Should handle RuntimeException with 'not found' message")
    void handleRuntimeException_WithNotFound_ShouldReturn400() {
        // Given
        RuntimeException ex = new RuntimeException("User not found");

        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleRuntimeException(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("Should handle generic RuntimeException")
    void handleRuntimeException_Generic_ShouldReturn500() {
        // Given
        RuntimeException ex = new RuntimeException("Some unexpected error");

        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleRuntimeException(ex);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_ERROR", response.getBody().getError().getCode());
    }

    @Test
    @DisplayName("Should handle generic Exception")
    void handleGenericException_ShouldReturn500() {
        // Given
        Exception ex = new Exception("Unexpected error");

        // When
        ResponseEntity<ApiResponse<Object>> response = exceptionHandler.handleGenericException(ex);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_ERROR", response.getBody().getError().getCode());
    }

    @Test
    @DisplayName("Should handle MethodArgumentNotValidException")
    void handleValidationExceptions_ShouldReturn400() {
        // Given
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("request", "username", "Username is required");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // When
        ResponseEntity<ApiResponse<Map<String, String>>> response = exceptionHandler.handleValidationExceptions(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VALIDATION_ERROR", response.getBody().getError().getCode());
    }
}
