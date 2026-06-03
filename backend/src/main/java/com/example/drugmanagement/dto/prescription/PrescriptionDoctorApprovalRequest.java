package com.example.drugmanagement.dto.prescription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PrescriptionDoctorApprovalRequest {

    @NotBlank(message = "must not be blank")
    private String action;

    @NotBlank(message = "must not be blank")
    private String doctorName;

    @NotNull(message = "must not be null")
    private Long doctorId;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
}
