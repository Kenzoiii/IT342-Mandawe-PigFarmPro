package com.it342.g3.backend.common.exception;

import com.it342.g3.backend.common.dto.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.persistence.EntityNotFoundException;

/**
 * Global Exception Handler for the application
 * Centralizes exception handling and maps exceptions to appropriate HTTP status codes
 * Ensures consistent API error responses across all endpoints
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handle DataIntegrityViolationException (Database constraint violations)
     * Maps to: 409 Conflict
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex) {
        
        String message = extractViolationMessage(ex);
        ApiResponse.ErrorDetails error = new ApiResponse.ErrorDetails(
            "DB-001",
            "Database constraint violation: " + message
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            new ApiResponse<Object>(false, null, "Duplicate entry detected", error)
        );
    }

    /**
     * Handle IllegalArgumentException (Validation errors)
     * Maps to: 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgument(
            IllegalArgumentException ex) {
        
        ApiResponse.ErrorDetails error = new ApiResponse.ErrorDetails(
            "VALID-001",
            ex.getMessage()
        );
        
        return ResponseEntity.badRequest().body(
            new ApiResponse<Object>(false, null, "Validation failed", error)
        );
    }

    /**
     * Handle EntityNotFoundException (Resource not found)
     * Maps to: 404 Not Found
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ApiResponse<Object>> handleEntityNotFound(
            EntityNotFoundException ex) {
        
        ApiResponse.ErrorDetails error = new ApiResponse.ErrorDetails(
            "NOT-FOUND",
            ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            new ApiResponse<Object>(false, null, "Resource not found", error)
        );
    }

    /**
     * Handle NullPointerException (Programming errors)
     * Maps to: 500 Internal Server Error
     */
    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiResponse<Object>> handleNullPointer(
            NullPointerException ex) {
        
        ApiResponse.ErrorDetails error = new ApiResponse.ErrorDetails(
            "SYSTEM-002",
            "Null pointer exception in application logic"
        );
        
        ex.printStackTrace(); // Log for debugging
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            new ApiResponse<Object>(false, null, "Internal server error", error)
        );
    }

    /**
     * Handle all other exceptions (Generic fallback)
     * Maps to: 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex) {
        
        ApiResponse.ErrorDetails error = new ApiResponse.ErrorDetails(
            "SYSTEM-001",
            "Internal server error: " + ex.getClass().getSimpleName()
        );
        
        ex.printStackTrace(); // Log for debugging
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            new ApiResponse<Object>(false, null, "An unexpected error occurred", error)
        );
    }

    /**
     * Extract violation message from DataIntegrityViolationException
     * Handles both duplicate key and unique constraint violations
     */
    private String extractViolationMessage(DataIntegrityViolationException ex) {
        Throwable cause = ex.getCause();
        if (cause != null && cause.getMessage() != null) {
            String message = cause.getMessage();
            if (message.contains("duplicate key") || message.contains("Duplicate entry")) {
                return "Duplicate entry - this record already exists";
            } else if (message.contains("unique")) {
                return "Unique constraint violation";
            }
            return message;
        }
        return "Database constraint violation";
    }
}
