package com.it342.g3.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mortality_records")
@Data
public class MortalityRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mortality_record_id")
    private Long mortalityRecordId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pig_id", unique = true, nullable = false)
    private Pig pig;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by", nullable = false)
    private User recordedBy;

    @Column(name = "date_of_death")
    private LocalDate dateOfDeath;

    @Column(name = "age_at_death")
    private Integer ageAtDeath;

    @Column(name = "cause_of_death", length = 150)
    private String causeOfDeath;

    @Column(name = "weight_at_death", precision = 10, scale = 2)
    private BigDecimal weightAtDeath;

    @Column(name = "symptoms", columnDefinition = "TEXT")
    private String symptoms;

    @Column(name = "actions_taken", columnDefinition = "TEXT")
    private String actionsTaken;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "recorded_at", updatable = false)
    private LocalDateTime recordedAt;

    @PrePersist
    protected void onCreate() {
        recordedAt = LocalDateTime.now();
    }
}
