package com.it342.g3.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreatePigRequest {
    private String pigIdentifier;
    private String breed;
    private LocalDate birthdate;
    private BigDecimal currentWeight;
    private String weightUnit;
    private String gender;
    private String status;
    private String notes;

    public CreatePigRequest() {}

    public String getPigIdentifier() {
        return pigIdentifier;
    }

    public void setPigIdentifier(String pigIdentifier) {
        this.pigIdentifier = pigIdentifier;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public BigDecimal getCurrentWeight() {
        return currentWeight;
    }

    public void setCurrentWeight(BigDecimal currentWeight) {
        this.currentWeight = currentWeight;
    }

    public String getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(String weightUnit) {
        this.weightUnit = weightUnit;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
