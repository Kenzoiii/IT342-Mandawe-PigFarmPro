package com.it342.g3.backend.dto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private ErrorDetails error;
    private String timestamp;

    public ApiResponse() {
        this.timestamp = formatTimestamp(LocalDateTime.now());
    }

    public ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.timestamp = formatTimestamp(LocalDateTime.now());
    }

    public ApiResponse(boolean success, T data, String message, ErrorDetails error) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.error = error;
        this.timestamp = formatTimestamp(LocalDateTime.now());
    }

    public ApiResponse(boolean success, String message, ErrorDetails error) {
        this.success = success;
        this.message = message;
        this.error = error;
        this.timestamp = formatTimestamp(LocalDateTime.now());
    }

    private String formatTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.of("UTC"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")) + ".000Z";
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    // Inner class for error details
    public static class ErrorDetails {
        private String code;
        private String message;
        private Object details;

        public ErrorDetails() {}

        public ErrorDetails(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public ErrorDetails(String code, String message, Object details) {
            this.code = code;
            this.message = message;
            this.details = details;
        }

        // Getters and Setters
        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getDetails() {
            return details;
        }

        public void setDetails(Object details) {
            this.details = details;
        }
    }
}
