package com.it342.g3.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CreateFeedingRequest {
    private Long penId;
    private String feedType;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal cost;
    private LocalDateTime feedingTime;
    private String notes;

    public CreateFeedingRequest() {}

    public Long getPenId() {
        return penId;
    }

    public void setPenId(Long penId) {
        this.penId = penId;
    }

    public String getFeedType() {
        return feedType;
    }

    public void setFeedType(String feedType) {
        this.feedType = feedType;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public void setCost(BigDecimal cost) {
        this.cost = cost;
    }

    public LocalDateTime getFeedingTime() {
        return feedingTime;
    }

    public void setFeedingTime(LocalDateTime feedingTime) {
        this.feedingTime = feedingTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
