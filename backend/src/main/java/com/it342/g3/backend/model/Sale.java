package com.it342.g3.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
@Data
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sale_id")
    private Long saleId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pig_id", unique = true, nullable = false)
    private Pig pig;

    @Column(name = "buyer_name", length = 150)
    private String buyerName;

    @Column(name = "buyer_contact", length = 100)
    private String buyerContact;

    @Column(name = "sale_price", precision = 12, scale = 2)
    private BigDecimal salePrice;

    @Column(name = "sale_date")
    private LocalDate saleDate;

    @Column(name = "expected_pickup_date")
    private LocalDate expectedPickupDate;

    @Column(name = "actual_pickup_date")
    private LocalDate actualPickupDate;

    @Column(name = "status", length = 30)
    private String status;

    @Column(name = "payment_status", length = 30)
    private String paymentStatus;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
