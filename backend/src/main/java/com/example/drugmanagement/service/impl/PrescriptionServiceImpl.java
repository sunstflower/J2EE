package com.example.drugmanagement.service.impl;

import com.example.drugmanagement.common.auth.CurrentUser;
import com.example.drugmanagement.common.auth.CurrentUserHolder;
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
import com.example.drugmanagement.vo.prescription.PrescriptionVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PrescriptionServiceImpl implements PrescriptionService {

    private static final DateTimeFormatter PRESCRIPTION_NO_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final InventoryMapper inventoryMapper;
    private final InventoryRecordMapper inventoryRecordMapper;
    private final DrugMapper drugMapper;

    public PrescriptionServiceImpl(PrescriptionMapper prescriptionMapper,
                                   PrescriptionItemMapper prescriptionItemMapper,
                                   InventoryMapper inventoryMapper,
                                   InventoryRecordMapper inventoryRecordMapper,
                                   DrugMapper drugMapper) {
        this.prescriptionMapper = prescriptionMapper;
        this.prescriptionItemMapper = prescriptionItemMapper;
        this.inventoryMapper = inventoryMapper;
        this.inventoryRecordMapper = inventoryRecordMapper;
        this.drugMapper = drugMapper;
    }

    @Override
    @Transactional
    public Long createPrescription(CreatePrescriptionRequest request) {
        CurrentUser currentUser = requireCurrentUser();
        if (currentUser.role() != RoleType.DOCTOR) {
            throw BusinessException.of(ResponseCode.UNAUTHORIZED);
        }

        Prescription prescription = new Prescription();
        prescription.setPrescriptionNo("RX-" + LocalDateTime.now().format(PRESCRIPTION_NO_FORMAT));
        prescription.setPatientName(request.getPatientName());
        prescription.setCreatedByUserId(currentUser.userId());
        prescription.setCreatedByRole(currentUser.role().name());
        prescription.setDoctorId(currentUser.userId());
        prescription.setDoctorName(currentUser.userName());
        prescription.setStatus(PrescriptionStatus.APPROVED.name());
        prescription.setDoctorApprovalStatus(DoctorApprovalStatus.APPROVED.name());
        prescription.setDoctorApprovedAt(LocalDateTime.now());
        prescription.setRemark(request.getRemark());
        prescription.setCreatedBy(currentUser.userName());
        prescription.setUpdatedBy(currentUser.userName());
        prescription.setDeleted(0);
        prescriptionMapper.insert(prescription);

        List<PrescriptionItem> items = new ArrayList<>();
        for (PrescriptionItemRequest requestItem : request.getItems()) {
            ensureDrugExists(requestItem.getDrugId());
            PrescriptionItem item = new PrescriptionItem();
            item.setPrescriptionId(prescription.getId());
            item.setDrugId(requestItem.getDrugId());
            item.setDosage(requestItem.getDosage());
            item.setFrequency(requestItem.getFrequency());
            item.setDays(requestItem.getDays());
            item.setQuantity(requestItem.getQuantity());
            item.setCreatedBy(currentUser.userName());
            item.setUpdatedBy(currentUser.userName());
            item.setDeleted(0);
            items.add(item);
        }
        prescriptionItemMapper.insertBatch(items);
        dispensePrescriptionItems(prescription, items, currentUser.userName());
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
        for (PrescriptionVO record : records) {
            record.setItems(prescriptionItemMapper.findByPrescriptionId(record.getId()));
        }
        long total = prescriptionMapper.count(request);
        return PageResponse.of(records, total, request.getPageNum(), request.getPageSize());
    }

    @Override
    @Transactional
    public void doctorApprove(Long id, PrescriptionDoctorApprovalRequest request) {
        Prescription prescription = getExistingPrescription(id);
        String status = request.getApproved() ? PrescriptionStatus.APPROVED.name() : PrescriptionStatus.REJECTED.name();
        String approvalStatus = request.getApproved() ? DoctorApprovalStatus.APPROVED.name() : DoctorApprovalStatus.REJECTED.name();
        prescriptionMapper.updateStatus(
                prescription.getId(),
                status,
                approvalStatus,
                LocalDateTime.now(),
                prescription.getAuditBy(),
                prescription.getAuditTime(),
                prescription.getDispenseBy(),
                prescription.getDispenseTime(),
                request.getRejectReason(),
                currentUserName()
        );
    }

    @Override
    @Transactional
    public void submit(Long id) {
        Prescription prescription = getExistingPrescription(id);
        prescriptionMapper.updateStatus(
                prescription.getId(),
                PrescriptionStatus.SUBMITTED.name(),
                prescription.getDoctorApprovalStatus(),
                prescription.getDoctorApprovedAt(),
                prescription.getAuditBy(),
                prescription.getAuditTime(),
                prescription.getDispenseBy(),
                prescription.getDispenseTime(),
                prescription.getRejectReason(),
                currentUserName()
        );
    }

    @Override
    @Transactional
    public void audit(Long id, PrescriptionAuditRequest request) {
        Prescription prescription = getExistingPrescription(id);
        String status = request.getApproved() ? PrescriptionStatus.APPROVED.name() : PrescriptionStatus.REJECTED.name();
        prescriptionMapper.updateStatus(
                prescription.getId(),
                status,
                prescription.getDoctorApprovalStatus(),
                prescription.getDoctorApprovedAt(),
                currentUserName(),
                LocalDateTime.now(),
                prescription.getDispenseBy(),
                prescription.getDispenseTime(),
                request.getRejectReason(),
                currentUserName()
        );
    }

    @Override
    @Transactional
    public void dispense(Long id, PrescriptionDispenseRequest request) {
        Prescription prescription = getExistingPrescription(id);
        prescriptionMapper.updateStatus(
                prescription.getId(),
                PrescriptionStatus.DISPENSED.name(),
                prescription.getDoctorApprovalStatus(),
                prescription.getDoctorApprovedAt(),
                prescription.getAuditBy(),
                prescription.getAuditTime(),
                currentUserName(),
                LocalDateTime.now(),
                request.getRemark(),
                currentUserName()
        );
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        Prescription prescription = getExistingPrescription(id);
        prescriptionMapper.updateStatus(
                prescription.getId(),
                PrescriptionStatus.CANCELLED.name(),
                prescription.getDoctorApprovalStatus(),
                prescription.getDoctorApprovedAt(),
                prescription.getAuditBy(),
                prescription.getAuditTime(),
                prescription.getDispenseBy(),
                prescription.getDispenseTime(),
                prescription.getRejectReason(),
                currentUserName()
        );
    }

    private void dispensePrescriptionItems(Prescription prescription,
                                           List<PrescriptionItem> items,
                                           String operatorName) {
        for (PrescriptionItem item : items) {
            int remainingQuantity = item.getQuantity();
            List<Inventory> inventories = inventoryMapper.findAvailableByDrugIdOrderByExpiry(item.getDrugId());
            for (Inventory inventory : inventories) {
                if (remainingQuantity <= 0) {
                    break;
                }
                int consumeQuantity = Math.min(remainingQuantity, inventory.getQuantity());
                int affectedRows = inventoryMapper.decreaseQuantity(inventory.getId(), consumeQuantity, operatorName);
                if (affectedRows == 0) {
                    continue;
                }
                writeDispenseRecord(prescription, item, inventory, consumeQuantity, operatorName);
                remainingQuantity -= consumeQuantity;
            }
            if (remainingQuantity > 0) {
                throw BusinessException.of(ResponseCode.BUSINESS_RULE_VIOLATION);
            }
        }

        prescriptionMapper.updateStatus(
                prescription.getId(),
                PrescriptionStatus.DISPENSED.name(),
                prescription.getDoctorApprovalStatus(),
                prescription.getDoctorApprovedAt(),
                prescription.getAuditBy(),
                prescription.getAuditTime(),
                operatorName,
                LocalDateTime.now(),
                prescription.getRejectReason(),
                operatorName
        );
    }

    private void writeDispenseRecord(Prescription prescription,
                                     PrescriptionItem item,
                                     Inventory inventory,
                                     int consumeQuantity,
                                     String operatorName) {
        InventoryRecord inventoryRecord = new InventoryRecord();
        inventoryRecord.setDrugId(item.getDrugId());
        inventoryRecord.setInventoryId(inventory.getId());
        inventoryRecord.setRecordType(InventoryRecordType.DISPENSE.name());
        inventoryRecord.setQuantityChange(-consumeQuantity);
        inventoryRecord.setBeforeQuantity(inventory.getQuantity());
        inventoryRecord.setAfterQuantity(inventory.getQuantity() - consumeQuantity);
        inventoryRecord.setBizNo(prescription.getPrescriptionNo());
        inventoryRecord.setOperatorName(operatorName);
        inventoryRecord.setOperatedAt(LocalDateTime.now());
        inventoryRecord.setRemark("处方发药");
        inventoryRecord.setCreatedBy(operatorName);
        inventoryRecord.setUpdatedBy(operatorName);
        inventoryRecord.setDeleted(0);
        inventoryRecordMapper.insert(inventoryRecord);
    }

    private void ensureDrugExists(Long drugId) {
        if (drugMapper.findEntityById(drugId) == null) {
            throw BusinessException.of(ResponseCode.RESOURCE_NOT_FOUND);
        }
    }

    private Prescription getExistingPrescription(Long id) {
        Prescription prescription = prescriptionMapper.findEntityById(id);
        if (prescription == null) {
            throw BusinessException.of(ResponseCode.RESOURCE_NOT_FOUND);
        }
        return prescription;
    }

    private CurrentUser requireCurrentUser() {
        CurrentUser currentUser = CurrentUserHolder.get();
        if (currentUser == null) {
            throw BusinessException.of(ResponseCode.UNAUTHORIZED);
        }
        return currentUser;
    }

    private String currentUserName() {
        CurrentUser currentUser = CurrentUserHolder.get();
        return currentUser == null ? "system" : currentUser.userName();
    }
}
