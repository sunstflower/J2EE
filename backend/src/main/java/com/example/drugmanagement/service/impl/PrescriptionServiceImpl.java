package com.example.drugmanagement.service.impl;

import com.example.drugmanagement.common.enums.DoctorApprovalStatus;
import com.example.drugmanagement.common.enums.InventoryRecordType;
import com.example.drugmanagement.common.enums.PrescriptionStatus;
import com.example.drugmanagement.common.enums.RoleType;
import com.example.drugmanagement.common.exception.BusinessException;
import com.example.drugmanagement.common.response.PageResponse;
import com.example.drugmanagement.common.response.ResponseCode;
import com.example.drugmanagement.dto.prescription.CreatePrescriptionRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionAuditRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionDispenseRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionDoctorApprovalRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionItemRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionQueryRequest;
import com.example.drugmanagement.entity.Drug;
import com.example.drugmanagement.entity.Inventory;
import com.example.drugmanagement.entity.InventoryRecord;
import com.example.drugmanagement.entity.Prescription;
import com.example.drugmanagement.entity.PrescriptionItem;
import com.example.drugmanagement.mapper.DrugMapper;
import com.example.drugmanagement.mapper.InventoryMapper;
import com.example.drugmanagement.mapper.InventoryRecordMapper;
import com.example.drugmanagement.mapper.PrescriptionItemMapper;
import com.example.drugmanagement.mapper.PrescriptionMapper;
import com.example.drugmanagement.service.PrescriptionService;
import com.example.drugmanagement.vo.prescription.PrescriptionItemVO;
import com.example.drugmanagement.vo.prescription.PrescriptionVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PrescriptionServiceImpl implements PrescriptionService {

    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final DrugMapper drugMapper;
    private final InventoryMapper inventoryMapper;
    private final InventoryRecordMapper inventoryRecordMapper;

    public PrescriptionServiceImpl(PrescriptionMapper prescriptionMapper,
                                   PrescriptionItemMapper prescriptionItemMapper,
                                   DrugMapper drugMapper,
                                   InventoryMapper inventoryMapper,
                                   InventoryRecordMapper inventoryRecordMapper) {
        this.prescriptionMapper = prescriptionMapper;
        this.prescriptionItemMapper = prescriptionItemMapper;
        this.drugMapper = drugMapper;
        this.inventoryMapper = inventoryMapper;
        this.inventoryRecordMapper = inventoryRecordMapper;
    }

    @Override
    @Transactional
    public Long createPrescription(CreatePrescriptionRequest request) {
        RoleType roleType = RoleType.valueOf(request.getCreatedByRole());
        validateCreateRoleRules(request, roleType);
        validatePrescriptionItems(request.getItems());

        Prescription prescription = new Prescription();
        prescription.setPrescriptionNo(generatePrescriptionNo());
        prescription.setPatientName(request.getPatientName());
        prescription.setCreatedByUserId(request.getCreatedByUserId());
        prescription.setCreatedByRole(request.getCreatedByRole());
        prescription.setDoctorId(request.getDoctorId());
        prescription.setDoctorName(request.getDoctorName());
        prescription.setCreatedBy(request.getCreatedByName());
        prescription.setUpdatedBy(request.getCreatedByName());
        prescription.setDeleted(0);

        if (roleType == RoleType.DOCTOR) {
            prescription.setStatus(PrescriptionStatus.DRAFT.name());
            prescription.setDoctorApprovalStatus(DoctorApprovalStatus.NONE.name());
        } else {
            prescription.setStatus(PrescriptionStatus.PENDING_DOCTOR_APPROVAL.name());
            prescription.setDoctorApprovalStatus(DoctorApprovalStatus.PENDING.name());
            prescription.setPharmacistOperatorId(request.getCreatedByUserId());
        }

        prescriptionMapper.insert(prescription);

        List<PrescriptionItem> items = new ArrayList<>();
        for (PrescriptionItemRequest itemRequest : request.getItems()) {
            PrescriptionItem item = new PrescriptionItem();
            item.setPrescriptionId(prescription.getId());
            item.setDrugId(itemRequest.getDrugId());
            item.setDosage(itemRequest.getDosage());
            item.setFrequency(itemRequest.getFrequency());
            item.setDays(itemRequest.getDays());
            item.setQuantity(itemRequest.getQuantity());
            item.setCreatedBy(request.getCreatedByName());
            item.setUpdatedBy(request.getCreatedByName());
            item.setDeleted(0);
            items.add(item);
        }
        prescriptionItemMapper.insertBatch(items);
        return prescription.getId();
    }

    @Override
    public PrescriptionVO getPrescriptionById(Long id) {
        PrescriptionVO prescriptionVO = prescriptionMapper.findById(id);
        if (prescriptionVO == null) {
            throw BusinessException.of(ResponseCode.RESOURCE_NOT_FOUND);
        }
        prescriptionVO.setItems(prescriptionItemMapper.findByPrescriptionId(id));
        return prescriptionVO;
    }

    @Override
    public PageResponse<PrescriptionVO> queryPrescriptions(PrescriptionQueryRequest request) {
        List<PrescriptionVO> records = prescriptionMapper.findPage(request);
        long total = prescriptionMapper.count(request);
        return PageResponse.of(records, total, request.getPageNum(), request.getPageSize());
    }

    @Override
    @Transactional
    public void doctorApprove(Long id, PrescriptionDoctorApprovalRequest request) {
        Prescription prescription = getExistingPrescription(id);
        if (!PrescriptionStatus.PENDING_DOCTOR_APPROVAL.name().equals(prescription.getStatus())) {
            throw BusinessException.of(ResponseCode.BUSINESS_RULE_VIOLATION);
        }
        if (!prescription.getDoctorId().equals(request.getDoctorId())) {
            throw BusinessException.of(ResponseCode.BUSINESS_RULE_VIOLATION);
        }

        if ("APPROVE".equals(request.getAction())) {
            prescriptionMapper.updateStatus(
                    id,
                    PrescriptionStatus.SUBMITTED.name(),
                    DoctorApprovalStatus.APPROVED.name(),
                    LocalDateTime.now(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    request.getDoctorName()
            );
        } else if ("REJECT".equals(request.getAction())) {
            prescriptionMapper.updateStatus(
                    id,
                    PrescriptionStatus.CANCELLED.name(),
                    DoctorApprovalStatus.REJECTED.name(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    "doctor rejected proxy prescription",
                    request.getDoctorName()
            );
        } else {
            throw BusinessException.of(ResponseCode.BUSINESS_RULE_VIOLATION);
        }
    }

    @Override
    @Transactional
    public void submit(Long id) {
        Prescription prescription = getExistingPrescription(id);
        if (RoleType.DOCTOR.name().equals(prescription.getCreatedByRole())) {
            if (!PrescriptionStatus.DRAFT.name().equals(prescription.getStatus())) {
                throw BusinessException.of(ResponseCode.BUSINESS_RULE_VIOLATION);
            }
        } else {
            if (!PrescriptionStatus.SUBMITTED.name().equals(prescription.getStatus())) {
                throw BusinessException.of(ResponseCode.BUSINESS_RULE_VIOLATION);
            }
            return;
        }

        prescriptionMapper.updateStatus(
                id,
                PrescriptionStatus.SUBMITTED.name(),
                prescription.getDoctorApprovalStatus(),
                prescription.getDoctorApprovedAt(),
                null,
                null,
                null,
                null,
                null,
                prescription.getUpdatedBy()
        );
    }

    @Override
    @Transactional
    public void audit(Long id, PrescriptionAuditRequest request) {
        Prescription prescription = getExistingPrescription(id);
        if (!PrescriptionStatus.SUBMITTED.name().equals(prescription.getStatus())) {
            throw BusinessException.of(ResponseCode.BUSINESS_RULE_VIOLATION);
        }

        if ("APPROVE".equals(request.getAction())) {
            prescriptionMapper.updateStatus(
                    id,
                    PrescriptionStatus.APPROVED.name(),
                    prescription.getDoctorApprovalStatus(),
                    prescription.getDoctorApprovedAt(),
                    request.getOperatorName(),
                    LocalDateTime.now(),
                    null,
                    null,
                    null,
                    request.getOperatorName()
            );
        } else if ("REJECT".equals(request.getAction())) {
            prescriptionMapper.updateStatus(
                    id,
                    PrescriptionStatus.REJECTED.name(),
                    prescription.getDoctorApprovalStatus(),
                    prescription.getDoctorApprovedAt(),
                    request.getOperatorName(),
                    LocalDateTime.now(),
                    null,
                    null,
                    request.getRejectReason(),
                    request.getOperatorName()
            );
        } else {
            throw BusinessException.of(ResponseCode.BUSINESS_RULE_VIOLATION);
        }
    }

    @Override
    @Transactional
    public void dispense(Long id, PrescriptionDispenseRequest request) {
        Prescription prescription = getExistingPrescription(id);
        if (!PrescriptionStatus.APPROVED.name().equals(prescription.getStatus())) {
            throw BusinessException.of(ResponseCode.BUSINESS_RULE_VIOLATION);
        }

        List<PrescriptionItemVO> items = prescriptionItemMapper.findByPrescriptionId(id);
        for (PrescriptionItemVO item : items) {
            Drug drug = drugMapper.findEntityById(item.getDrugId());
            if (drug == null || drug.getEnabled() == null || drug.getEnabled() != 1) {
                throw BusinessException.of(ResponseCode.BUSINESS_RULE_VIOLATION);
            }

            List<Inventory> inventories = inventoryMapper.findAvailableByDrugIdOrderByExpiry(item.getDrugId());
            int remaining = item.getQuantity();

            for (Inventory inventory : inventories) {
                if (remaining <= 0) {
                    break;
                }
                if (inventory.getExpiryDate() != null && inventory.getExpiryDate().isBefore(java.time.LocalDate.now())) {
                    continue;
                }
                int available = inventory.getQuantity() - inventory.getLockedQuantity();
                if (available <= 0) {
                    continue;
                }

                int deduction = Math.min(available, remaining);
                int beforeQuantity = inventory.getQuantity();
                int afterQuantity = beforeQuantity - deduction;
                inventoryMapper.decreaseQuantity(inventory.getId(), deduction, request.getOperatorName());

                InventoryRecord record = new InventoryRecord();
                record.setDrugId(item.getDrugId());
                record.setInventoryId(inventory.getId());
                record.setRecordType(InventoryRecordType.DISPENSE.name());
                record.setQuantityChange(-deduction);
                record.setBeforeQuantity(beforeQuantity);
                record.setAfterQuantity(afterQuantity);
                record.setBizNo(prescription.getPrescriptionNo());
                record.setOperatorName(request.getOperatorName());
                record.setOperatedAt(LocalDateTime.now());
                record.setRemark("prescription dispense");
                record.setCreatedBy(request.getOperatorName());
                record.setUpdatedBy(request.getOperatorName());
                record.setDeleted(0);
                inventoryRecordMapper.insert(record);

                remaining -= deduction;
            }

            if (remaining > 0) {
                throw BusinessException.of(ResponseCode.BUSINESS_RULE_VIOLATION);
            }
        }

        int updatedRows = prescriptionMapper.updateStatusByCurrentStatus(
                id,
                PrescriptionStatus.APPROVED.name(),
                PrescriptionStatus.DISPENSED.name(),
                prescription.getDoctorApprovalStatus(),
                prescription.getDoctorApprovedAt(),
                prescription.getAuditBy(),
                prescription.getAuditTime(),
                request.getOperatorName(),
                LocalDateTime.now(),
                null,
                request.getOperatorName()
        );

        if (updatedRows != 1) {
            throw BusinessException.of(ResponseCode.BUSINESS_RULE_VIOLATION);
        }
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Prescription prescription = getExistingPrescription(id);
        if (PrescriptionStatus.DISPENSED.name().equals(prescription.getStatus())) {
            throw BusinessException.of(ResponseCode.BUSINESS_RULE_VIOLATION);
        }

        prescriptionMapper.updateStatus(
                id,
                PrescriptionStatus.CANCELLED.name(),
                prescription.getDoctorApprovalStatus(),
                prescription.getDoctorApprovedAt(),
                prescription.getAuditBy(),
                prescription.getAuditTime(),
                prescription.getDispenseBy(),
                prescription.getDispenseTime(),
                prescription.getRejectReason(),
                prescription.getUpdatedBy()
        );
    }

    private Prescription getExistingPrescription(Long id) {
        Prescription prescription = prescriptionMapper.findEntityById(id);
        if (prescription == null) {
            throw BusinessException.of(ResponseCode.RESOURCE_NOT_FOUND);
        }
        return prescription;
    }

    private void validateCreateRoleRules(CreatePrescriptionRequest request, RoleType roleType) {
        if (roleType == RoleType.DOCTOR) {
            if (!request.getCreatedByUserId().equals(request.getDoctorId())) {
                throw BusinessException.of(ResponseCode.BUSINESS_RULE_VIOLATION);
            }
        } else if (roleType == RoleType.PHARMACIST) {
            if (request.getCreatedByUserId().equals(request.getDoctorId())) {
                throw BusinessException.of(ResponseCode.BUSINESS_RULE_VIOLATION);
            }
        } else {
            throw BusinessException.of(ResponseCode.BUSINESS_RULE_VIOLATION);
        }
    }

    private void validatePrescriptionItems(List<PrescriptionItemRequest> items) {
        for (PrescriptionItemRequest item : items) {
            Drug drug = drugMapper.findEntityById(item.getDrugId());
            if (drug == null || drug.getEnabled() == null || drug.getEnabled() != 1) {
                throw BusinessException.of(ResponseCode.BUSINESS_RULE_VIOLATION);
            }
        }
    }

    private String generatePrescriptionNo() {
        return "RX-" + System.currentTimeMillis();
    }
}
