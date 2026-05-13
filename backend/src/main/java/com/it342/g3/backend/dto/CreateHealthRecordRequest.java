package com.it342.g3.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CreateHealthRecordRequest {
    private Long pigId;
    private String pigIdentifier;
    private BigDecimal weight;
    private String healthCondition;
    private BigDecimal temperature;
    private String treatmentGiven;
    private String medicationUsed;
    private LocalDate nextTreatmentDate;
    private String nextTreatmentType;
    private LocalDateTime checkupDate;
    private String notes;

    public CreateHealthRecordRequest() {}

    public Long getPigId() {
        return pigId;
    }

    public void setPigId(Long pigId) {
        this.pigId = pigId;
    }

    public String getPigIdentifier() {
        return pigIdentifier;
    }

    public void setPigIdentifier(String pigIdentifier) {
        this.pigIdentifier = pigIdentifier;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getHealthCondition() {
        return healthCondition;
    }

    public void setHealthCondition(String healthCondition) {
        this.healthCondition = healthCondition;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public String getTreatmentGiven() {
        return treatmentGiven;
    }

    public void setTreatmentGiven(String treatmentGiven) {
        this.treatmentGiven = treatmentGiven;
    }

    public String getMedicationUsed() {
        return medicationUsed;
    }

    public void setMedicationUsed(String medicationUsed) {
        this.medicationUsed = medicationUsed;
    }

    public LocalDate getNextTreatmentDate() {
        return nextTreatmentDate;
    }

    public void setNextTreatmentDate(LocalDate nextTreatmentDate) {
        this.nextTreatmentDate = nextTreatmentDate;
    }

    public String getNextTreatmentType() {
        return nextTreatmentType;
    }

    public void setNextTreatmentType(String nextTreatmentType) {
        this.nextTreatmentType = nextTreatmentType;
    }

    public LocalDateTime getCheckupDate() {
        return checkupDate;
    }

    public void setCheckupDate(LocalDateTime checkupDate) {
        this.checkupDate = checkupDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
