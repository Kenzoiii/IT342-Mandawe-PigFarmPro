package com.it342.g3.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class UpdateSaleRequest {
    private String buyerName;
    private String buyerContact;
    private BigDecimal salePrice;
    private LocalDate saleDate;
    private LocalDate expectedPickupDate;
    private LocalDate actualPickupDate;
    private String status;
    private String paymentStatus;
    private String notes;

    public UpdateSaleRequest() {}

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerContact() {
        return buyerContact;
    }

    public void setBuyerContact(String buyerContact) {
        this.buyerContact = buyerContact;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public LocalDate getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDate saleDate) {
        this.saleDate = saleDate;
    }

    public LocalDate getExpectedPickupDate() {
        return expectedPickupDate;
    }

    public void setExpectedPickupDate(LocalDate expectedPickupDate) {
        this.expectedPickupDate = expectedPickupDate;
    }

    public LocalDate getActualPickupDate() {
        return actualPickupDate;
    }

    public void setActualPickupDate(LocalDate actualPickupDate) {
        this.actualPickupDate = actualPickupDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
