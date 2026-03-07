package com.it342.g3.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "health_records")
@Data
public class HealthRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "health_record_id")
    private Long healthRecordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pig_id", nullable = false)
    private Pig pig;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by", nullable = false)
    private User recordedBy;

    @Column(name = "weight", precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(name = "health_condition", length = 50)
    private String healthCondition;

    @Column(name = "temperature", precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(name = "treatment_given", columnDefinition = "TEXT")
    private String treatmentGiven;

    @Column(name = "medication_used", length = 150)
    private String medicationUsed;

    @Column(name = "next_treatment_date")
    private LocalDate nextTreatmentDate;

    @Column(name = "next_treatment_type", length = 120)
    private String nextTreatmentType;

    @Column(name = "checkup_date")
    private LocalDateTime checkupDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
