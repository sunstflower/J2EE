package com.example.drugmanagement.entity;

import com.example.drugmanagement.common.entity.AuditEntity;
import java.time.LocalDateTime;

public class Prescription extends AuditEntity {

    private String prescriptionNo;
    private String patientName;
    private Long createdByUserId;
    private String createdByRole;
    private Long doctorId;
    private String doctorName;
    private String status;
    private String doctorApprovalStatus;
    private LocalDateTime doctorApprovedAt;
    private Long pharmacistOperatorId;
    private String auditBy;
    private LocalDateTime auditTime;
    private String dispenseBy;
    private LocalDateTime dispenseTime;
    private String rejectReason;

    public String getPrescriptionNo() {
        return prescriptionNo;
    }

    public void setPrescriptionNo(String prescriptionNo) {
        this.prescriptionNo = prescriptionNo;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public String getCreatedByRole() {
        return createdByRole;
    }

    public void setCreatedByRole(String createdByRole) {
        this.createdByRole = createdByRole;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDoctorApprovalStatus() {
        return doctorApprovalStatus;
    }

    public void setDoctorApprovalStatus(String doctorApprovalStatus) {
        this.doctorApprovalStatus = doctorApprovalStatus;
    }

    public LocalDateTime getDoctorApprovedAt() {
        return doctorApprovedAt;
    }

    public void setDoctorApprovedAt(LocalDateTime doctorApprovedAt) {
        this.doctorApprovedAt = doctorApprovedAt;
    }

    public Long getPharmacistOperatorId() {
        return pharmacistOperatorId;
    }

    public void setPharmacistOperatorId(Long pharmacistOperatorId) {
        this.pharmacistOperatorId = pharmacistOperatorId;
    }

    public String getAuditBy() {
        return auditBy;
    }

    public void setAuditBy(String auditBy) {
        this.auditBy = auditBy;
    }

    public LocalDateTime getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(LocalDateTime auditTime) {
        this.auditTime = auditTime;
    }

    public String getDispenseBy() {
        return dispenseBy;
    }

    public void setDispenseBy(String dispenseBy) {
        this.dispenseBy = dispenseBy;
    }

    public LocalDateTime getDispenseTime() {
        return dispenseTime;
    }

    public void setDispenseTime(LocalDateTime dispenseTime) {
        this.dispenseTime = dispenseTime;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}
