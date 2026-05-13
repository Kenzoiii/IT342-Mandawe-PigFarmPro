package com.it342.g3.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateMortalityRecordRequest {
    private Long pigId;
    private String pigIdentifier;
    private LocalDate dateOfDeath;
    private Integer ageAtDeath;
    private String causeOfDeath;
    private BigDecimal weightAtDeath;
    private String symptoms;
    private String actionsTaken;
    private String notes;

    public CreateMortalityRecordRequest() {}

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

    public LocalDate getDateOfDeath() {
        return dateOfDeath;
    }

    public void setDateOfDeath(LocalDate dateOfDeath) {
        this.dateOfDeath = dateOfDeath;
    }

    public Integer getAgeAtDeath() {
        return ageAtDeath;
    }

    public void setAgeAtDeath(Integer ageAtDeath) {
        this.ageAtDeath = ageAtDeath;
    }

    public String getCauseOfDeath() {
        return causeOfDeath;
    }

    public void setCauseOfDeath(String causeOfDeath) {
        this.causeOfDeath = causeOfDeath;
    }

    public BigDecimal getWeightAtDeath() {
        return weightAtDeath;
    }

    public void setWeightAtDeath(BigDecimal weightAtDeath) {
        this.weightAtDeath = weightAtDeath;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getActionsTaken() {
        return actionsTaken;
    }

    public void setActionsTaken(String actionsTaken) {
        this.actionsTaken = actionsTaken;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
