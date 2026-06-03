package com.example.drugmanagement.service;

import com.example.drugmanagement.common.response.PageResponse;
import com.example.drugmanagement.dto.prescription.CreatePrescriptionRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionAuditRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionDispenseRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionDoctorApprovalRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionQueryRequest;
import com.example.drugmanagement.vo.prescription.PrescriptionVO;

public interface PrescriptionService {

    Long createPrescription(CreatePrescriptionRequest request);

    PrescriptionVO getPrescriptionById(Long id);

    PageResponse<PrescriptionVO> queryPrescriptions(PrescriptionQueryRequest request);

    void doctorApprove(Long id, PrescriptionDoctorApprovalRequest request);

    void submit(Long id);

    void audit(Long id, PrescriptionAuditRequest request);

    void dispense(Long id, PrescriptionDispenseRequest request);

    void cancel(Long id);
}
