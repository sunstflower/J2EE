package com.example.drugmanagement.dto.prescription;

import com.example.drugmanagement.common.dto.PageQuery;

public class PrescriptionQueryRequest extends PageQuery {

    private String status;
    private Long doctorId;
    private String patientName;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }
}
