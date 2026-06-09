package com.example.drugmanagement.dto.prescription;

import jakarta.validation.constraints.NotNull;

public class PrescriptionAuditRequest {

    @NotNull(message = "must not be null")
    private Boolean approved;

    private String rejectReason;

    public Boolean getApproved() {
        return approved;
    }

    public void setApproved(Boolean approved) {
        this.approved = approved;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}
