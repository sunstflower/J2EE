package com.example.drugmanagement.controller;

import com.example.drugmanagement.common.response.ApiResponse;
import com.example.drugmanagement.common.response.PageResponse;
import com.example.drugmanagement.dto.prescription.CreatePrescriptionRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionAuditRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionDispenseRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionDoctorApprovalRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionQueryRequest;
import com.example.drugmanagement.service.PrescriptionService;
import com.example.drugmanagement.vo.prescription.PrescriptionVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @GetMapping
    public ApiResponse<PageResponse<PrescriptionVO>> queryPrescriptions(@Valid PrescriptionQueryRequest request) {
        return ApiResponse.success(prescriptionService.queryPrescriptions(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<PrescriptionVO> getPrescription(@PathVariable Long id) {
        return ApiResponse.success(prescriptionService.getPrescriptionById(id));
    }

    @PostMapping
    public ApiResponse<Long> createPrescription(@Valid @RequestBody CreatePrescriptionRequest request) {
        return ApiResponse.success(prescriptionService.createPrescription(request));
    }

    @PostMapping("/{id}/doctor-approve")
    public ApiResponse<Void> doctorApprove(@PathVariable Long id,
                                           @Valid @RequestBody PrescriptionDoctorApprovalRequest request) {
        prescriptionService.doctorApprove(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/submit")
    public ApiResponse<Void> submit(@PathVariable Long id) {
        prescriptionService.submit(id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/audit")
    public ApiResponse<Void> audit(@PathVariable Long id,
                                   @Valid @RequestBody PrescriptionAuditRequest request) {
        prescriptionService.audit(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/dispense")
    public ApiResponse<Void> dispense(@PathVariable Long id,
                                      @Valid @RequestBody PrescriptionDispenseRequest request) {
        prescriptionService.dispense(id, request);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        prescriptionService.cancel(id);
        return ApiResponse.success();
    }
}
