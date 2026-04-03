package com.it342.g3.backend.dto;

public class UpdatePenRequest {
    private String penIdentifier;
    private String penName;
    private Integer capacity;
    private String description;

    public UpdatePenRequest() {}

    public String getPenIdentifier() {
        return penIdentifier;
    }

    public void setPenIdentifier(String penIdentifier) {
        this.penIdentifier = penIdentifier;
    }

    public String getPenName() {
        return penName;
    }

    public void setPenName(String penName) {
        this.penName = penName;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
