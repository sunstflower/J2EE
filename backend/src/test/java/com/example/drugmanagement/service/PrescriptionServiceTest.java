package com.example.drugmanagement.service;

import com.example.drugmanagement.common.enums.DoctorApprovalStatus;
import com.example.drugmanagement.common.enums.PrescriptionStatus;
import com.example.drugmanagement.common.exception.BusinessException;
import com.example.drugmanagement.common.response.PageResponse;
import com.example.drugmanagement.dto.prescription.CreatePrescriptionRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionAuditRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionDispenseRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionDoctorApprovalRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionItemRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionQueryRequest;
import com.example.drugmanagement.entity.Drug;
import com.example.drugmanagement.entity.Inventory;
import com.example.drugmanagement.entity.Prescription;
import com.example.drugmanagement.mapper.DrugMapper;
import com.example.drugmanagement.mapper.InventoryMapper;
import com.example.drugmanagement.mapper.InventoryRecordMapper;
import com.example.drugmanagement.mapper.PrescriptionItemMapper;
import com.example.drugmanagement.mapper.PrescriptionMapper;
import com.example.drugmanagement.service.impl.PrescriptionServiceImpl;
import com.example.drugmanagement.vo.prescription.PrescriptionItemVO;
import com.example.drugmanagement.vo.prescription.PrescriptionVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PrescriptionServiceTest {

    @Mock
    private PrescriptionMapper prescriptionMapper;

    @Mock
    private PrescriptionItemMapper prescriptionItemMapper;

    @Mock
    private DrugMapper drugMapper;

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private InventoryRecordMapper inventoryRecordMapper;

    @InjectMocks
    private PrescriptionServiceImpl prescriptionService;

    @Test
    void shouldCreateDoctorPrescriptionAsDraft() {
        CreatePrescriptionRequest request = buildDoctorCreateRequest();
        Drug drug = enabledDrug(1L);
        when(drugMapper.findEntityById(1L)).thenReturn(drug);
        doAnswer(invocation -> {
            Prescription prescription = invocation.getArgument(0);
            prescription.setId(10L);
            return 1;
        }).when(prescriptionMapper).insert(any(Prescription.class));

        Long id = prescriptionService.createPrescription(request);

        assertEquals(10L, id);
        ArgumentCaptor<Prescription> captor = ArgumentCaptor.forClass(Prescription.class);
        verify(prescriptionMapper).insert(captor.capture());
        assertEquals(PrescriptionStatus.DRAFT.name(), captor.getValue().getStatus());
        assertEquals(DoctorApprovalStatus.NONE.name(), captor.getValue().getDoctorApprovalStatus());
    }

    @Test
    void shouldCreatePharmacistPrescriptionAsPendingDoctorApproval() {
        CreatePrescriptionRequest request = buildPharmacistCreateRequest();
        when(drugMapper.findEntityById(1L)).thenReturn(enabledDrug(1L));
        doAnswer(invocation -> {
            Prescription prescription = invocation.getArgument(0);
            prescription.setId(11L);
            return 1;
        }).when(prescriptionMapper).insert(any(Prescription.class));

        Long id = prescriptionService.createPrescription(request);

        assertEquals(11L, id);
        ArgumentCaptor<Prescription> captor = ArgumentCaptor.forClass(Prescription.class);
        verify(prescriptionMapper).insert(captor.capture());
        assertEquals(PrescriptionStatus.PENDING_DOCTOR_APPROVAL.name(), captor.getValue().getStatus());
        assertEquals(DoctorApprovalStatus.PENDING.name(), captor.getValue().getDoctorApprovalStatus());
    }

    @Test
    void shouldRejectPharmacistDirectPrescriptionWithoutDoctorBindingRule() {
        CreatePrescriptionRequest request = buildPharmacistCreateRequest();
        request.setDoctorId(200L);
        request.setCreatedByUserId(200L);

        assertThrows(BusinessException.class, () -> prescriptionService.createPrescription(request));
    }

    @Test
    void shouldApproveProxyPrescriptionByDoctor() {
        Prescription prescription = new Prescription();
        prescription.setId(1L);
        prescription.setStatus(PrescriptionStatus.PENDING_DOCTOR_APPROVAL.name());
        prescription.setDoctorId(100L);
        when(prescriptionMapper.findEntityById(1L)).thenReturn(prescription);

        PrescriptionDoctorApprovalRequest request = new PrescriptionDoctorApprovalRequest();
        request.setAction("APPROVE");
        request.setDoctorId(100L);
        request.setDoctorName("医生王");

        prescriptionService.doctorApprove(1L, request);

        verify(prescriptionMapper).updateStatus(eq(1L), eq(PrescriptionStatus.SUBMITTED.name()),
                eq(DoctorApprovalStatus.APPROVED.name()), any(), eq(null), eq(null), eq(null), eq(null), eq(null), eq("医生王"));
    }

    @Test
    void shouldAuditPrescriptionToApproved() {
        Prescription prescription = new Prescription();
        prescription.setId(1L);
        prescription.setStatus(PrescriptionStatus.SUBMITTED.name());
        when(prescriptionMapper.findEntityById(1L)).thenReturn(prescription);

        PrescriptionAuditRequest request = new PrescriptionAuditRequest();
        request.setAction("APPROVE");
        request.setOperatorId(300L);
        request.setOperatorName("药师李");

        prescriptionService.audit(1L, request);

        verify(prescriptionMapper).updateStatus(eq(1L), eq(PrescriptionStatus.APPROVED.name()),
                any(), any(), eq("药师李"), any(), eq(null), eq(null), eq(null), eq("药师李"));
    }

    @Test
    void shouldDispenseApprovedPrescription() {
        Prescription prescription = new Prescription();
        prescription.setId(1L);
        prescription.setPrescriptionNo("RX-1");
        prescription.setStatus(PrescriptionStatus.APPROVED.name());
        when(prescriptionMapper.findEntityById(1L)).thenReturn(prescription);
        PrescriptionItemVO item = new PrescriptionItemVO();
        item.setDrugId(1L);
        item.setQuantity(10);
        when(prescriptionItemMapper.findByPrescriptionId(1L)).thenReturn(List.of(item));
        when(drugMapper.findEntityById(1L)).thenReturn(enabledDrug(1L));
        Inventory inventory = new Inventory();
        inventory.setId(101L);
        inventory.setDrugId(1L);
        inventory.setQuantity(20);
        inventory.setLockedQuantity(0);
        inventory.setExpiryDate(java.time.LocalDate.now().plusDays(10));
        when(inventoryMapper.findAvailableByDrugIdOrderByExpiry(1L)).thenReturn(List.of(inventory));

        PrescriptionDispenseRequest request = new PrescriptionDispenseRequest();
        request.setOperatorId(300L);
        request.setOperatorName("药师李");

        when(prescriptionMapper.updateStatusByCurrentStatus(eq(1L), eq(PrescriptionStatus.APPROVED.name()),
                eq(PrescriptionStatus.DISPENSED.name()), any(), any(), any(), any(), eq("药师李"), any(), eq(null), eq("药师李")))
                .thenReturn(1);

        prescriptionService.dispense(1L, request);

        verify(inventoryMapper).decreaseQuantity(101L, 10, "药师李");
        verify(prescriptionMapper).updateStatusByCurrentStatus(eq(1L), eq(PrescriptionStatus.APPROVED.name()),
                eq(PrescriptionStatus.DISPENSED.name()), any(), any(), any(), any(), eq("药师李"), any(), eq(null), eq("药师李"));
    }

    @Test
    void shouldRejectDispenseWhenAllInventoryExpired() {
        Prescription prescription = new Prescription();
        prescription.setId(1L);
        prescription.setPrescriptionNo("RX-1");
        prescription.setStatus(PrescriptionStatus.APPROVED.name());
        when(prescriptionMapper.findEntityById(1L)).thenReturn(prescription);

        PrescriptionItemVO item = new PrescriptionItemVO();
        item.setDrugId(1L);
        item.setQuantity(10);
        when(prescriptionItemMapper.findByPrescriptionId(1L)).thenReturn(List.of(item));
        when(drugMapper.findEntityById(1L)).thenReturn(enabledDrug(1L));

        Inventory expiredInventory = new Inventory();
        expiredInventory.setId(101L);
        expiredInventory.setDrugId(1L);
        expiredInventory.setQuantity(20);
        expiredInventory.setLockedQuantity(0);
        expiredInventory.setExpiryDate(java.time.LocalDate.now().minusDays(1));
        when(inventoryMapper.findAvailableByDrugIdOrderByExpiry(1L)).thenReturn(List.of(expiredInventory));

        PrescriptionDispenseRequest request = new PrescriptionDispenseRequest();
        request.setOperatorId(300L);
        request.setOperatorName("药师李");

        assertThrows(BusinessException.class, () -> prescriptionService.dispense(1L, request));
        verify(inventoryMapper, never()).decreaseQuantity(anyLong(), any(), any());
    }

    @Test
    void shouldRejectDispenseWhenStatusTransitionFails() {
        Prescription prescription = new Prescription();
        prescription.setId(1L);
        prescription.setPrescriptionNo("RX-1");
        prescription.setStatus(PrescriptionStatus.APPROVED.name());
        when(prescriptionMapper.findEntityById(1L)).thenReturn(prescription);

        PrescriptionItemVO item = new PrescriptionItemVO();
        item.setDrugId(1L);
        item.setQuantity(5);
        when(prescriptionItemMapper.findByPrescriptionId(1L)).thenReturn(List.of(item));
        when(drugMapper.findEntityById(1L)).thenReturn(enabledDrug(1L));

        Inventory inventory = new Inventory();
        inventory.setId(101L);
        inventory.setDrugId(1L);
        inventory.setQuantity(20);
        inventory.setLockedQuantity(0);
        inventory.setExpiryDate(java.time.LocalDate.now().plusDays(5));
        when(inventoryMapper.findAvailableByDrugIdOrderByExpiry(1L)).thenReturn(List.of(inventory));
        when(prescriptionMapper.updateStatusByCurrentStatus(eq(1L), eq(PrescriptionStatus.APPROVED.name()),
                eq(PrescriptionStatus.DISPENSED.name()), any(), any(), any(), any(), eq("药师李"), any(), eq(null), eq("药师李")))
                .thenReturn(0);

        PrescriptionDispenseRequest request = new PrescriptionDispenseRequest();
        request.setOperatorId(300L);
        request.setOperatorName("药师李");

        assertThrows(BusinessException.class, () -> prescriptionService.dispense(1L, request));
    }

    @Test
    void shouldReturnPrescriptionPage() {
        PrescriptionQueryRequest request = new PrescriptionQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        PrescriptionVO prescriptionVO = new PrescriptionVO();
        prescriptionVO.setId(1L);
        when(prescriptionMapper.findPage(request)).thenReturn(List.of(prescriptionVO));
        when(prescriptionMapper.count(request)).thenReturn(1L);

        PageResponse<PrescriptionVO> response = prescriptionService.queryPrescriptions(request);

        assertEquals(1, response.records().size());
        assertEquals(1L, response.total());
    }

    private CreatePrescriptionRequest buildDoctorCreateRequest() {
        CreatePrescriptionRequest request = new CreatePrescriptionRequest();
        request.setPatientName("张三");
        request.setCreatedByRole("DOCTOR");
        request.setCreatedByUserId(100L);
        request.setCreatedByName("医生王");
        request.setDoctorId(100L);
        request.setDoctorName("医生王");
        request.setItems(List.of(buildItem()));
        return request;
    }

    private CreatePrescriptionRequest buildPharmacistCreateRequest() {
        CreatePrescriptionRequest request = new CreatePrescriptionRequest();
        request.setPatientName("李四");
        request.setCreatedByRole("PHARMACIST");
        request.setCreatedByUserId(200L);
        request.setCreatedByName("药师张");
        request.setDoctorId(100L);
        request.setDoctorName("医生王");
        request.setItems(List.of(buildItem()));
        return request;
    }

    private PrescriptionItemRequest buildItem() {
        PrescriptionItemRequest item = new PrescriptionItemRequest();
        item.setDrugId(1L);
        item.setDays(3);
        item.setQuantity(10);
        item.setDosage("1片");
        item.setFrequency("bid");
        return item;
    }

    private Drug enabledDrug(Long id) {
        Drug drug = new Drug();
        drug.setId(id);
        drug.setEnabled(1);
        return drug;
    }
}
