package com.it342.g3.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pigs")
@Data
public class Pig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pig_id")
    private Long pigId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pen_id", nullable = false)
    private Pen pen;

    @Column(name = "pig_identifier", unique = true, length = 80)
    private String pigIdentifier;

    @Column(name = "breed", length = 100)
    private String breed;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Column(name = "current_weight", precision = 10, scale = 2)
    private BigDecimal currentWeight;

    @Column(name = "weight_unit", length = 10)
    private String weightUnit;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
