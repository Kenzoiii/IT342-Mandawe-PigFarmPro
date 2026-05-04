package com.it342.g3.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard API Response DTO
 * Used for all API responses across the system
 * Vertical Slice: Common/Shared
 */
public class ApiResponse<T> {
    
    @JsonProperty("success")
    private boolean success;
    
    @JsonProperty("data")
    private T data;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("error")
    private ErrorDetails error;
    
    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    // Constructors
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(boolean success, T data, String message) {
        this();
        this.success = success;
        this.data = data;
        this.message = message;
    }

    public ApiResponse(boolean success, T data, String message, ErrorDetails error) {
        this();
        this.success = success;
        this.data = data;
        this.message = message;
        this.error = error;
    }

    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ErrorDetails getError() {
        return error;
    }

    public void setError(ErrorDetails error) {
        this.error = error;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Error Details nested class
     */
    public static class ErrorDetails {
        @JsonProperty("code")
        private String code;
        
        @JsonProperty("details")
        private String details;
        
        @JsonProperty("fields")
        private Map<String, String> fields;

        // Constructors
        public ErrorDetails() {}

        public ErrorDetails(String code, String details) {
            this.code = code;
            this.details = details;
        }

        public ErrorDetails(String code, String details, Map<String, String> fields) {
            this.code = code;
            this.details = details;
            this.fields = fields;
        }

        // Getters and Setters
        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDetails() {
            return details;
        }

        public void setDetails(String details) {
            this.details = details;
        }

        public Map<String, String> getFields() {
            return fields;
        }

        public void setFields(Map<String, String> fields) {
            this.fields = fields;
        }
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
