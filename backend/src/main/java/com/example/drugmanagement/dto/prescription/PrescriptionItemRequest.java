package com.example.drugmanagement.dto.prescription;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PrescriptionItemRequest {

    @NotNull(message = "must not be null")
    private Long drugId;

    private String dosage;
    private String frequency;

    @NotNull(message = "must not be null")
    @Min(value = 1, message = "must be greater than or equal to 1")
    private Integer days;

    @NotNull(message = "must not be null")
    @Min(value = 1, message = "must be greater than or equal to 1")
    private Integer quantity;

    public Long getDrugId() {
        return drugId;
    }

    public void setDrugId(Long drugId) {
        this.drugId = drugId;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
