package com.example.drugmanagement.service;

import com.example.drugmanagement.common.auth.CurrentUser;
import com.example.drugmanagement.common.auth.CurrentUserHolder;
import com.example.drugmanagement.common.enums.RoleType;
import com.example.drugmanagement.common.enums.InventoryRecordType;
import com.example.drugmanagement.common.enums.PrescriptionStatus;
import com.example.drugmanagement.dto.prescription.CreatePrescriptionRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionAuditRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionDispenseRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionDoctorApprovalRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionItemRequest;
import com.example.drugmanagement.dto.prescription.PrescriptionQueryRequest;
import com.example.drugmanagement.dto.inventory.InventoryRecordQueryRequest;
import com.example.drugmanagement.mapper.InventoryMapper;
import com.example.drugmanagement.service.PrescriptionService;
import com.example.drugmanagement.vo.inventory.InventoryRecordVO;
import com.example.drugmanagement.vo.inventory.InventoryVO;
import com.example.drugmanagement.vo.prescription.PrescriptionVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:prescription_flow;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
@Sql(scripts = "/h2/prescription-flow-init.sql")
class PrescriptionFlowIntegrationTest {

    @Autowired
    private PrescriptionService prescriptionService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryMapper inventoryMapper;

    @AfterEach
    void clearCurrentUser() {
        CurrentUserHolder.clear();
    }

    @Test
    void shouldCompleteProxyPrescriptionFlowAndWriteDispenseRecord() {
        CurrentUserHolder.set(new CurrentUser(200L, "药师张", RoleType.PHARMACIST));
        CreatePrescriptionRequest createRequest = new CreatePrescriptionRequest();
        createRequest.setPatientName("集成患者");
        createRequest.setCreatedByRole("PHARMACIST");
        createRequest.setCreatedByUserId(200L);
        createRequest.setCreatedByName("药师张");
        createRequest.setDoctorId(100L);
        createRequest.setDoctorName("王医生");
        createRequest.setItems(List.of(buildItem(1L, 12)));

        Long prescriptionId = prescriptionService.createPrescription(createRequest);

        PrescriptionVO created = prescriptionService.getPrescriptionById(prescriptionId);
        assertEquals(PrescriptionStatus.PENDING_DOCTOR_APPROVAL.name(), created.getStatus());
        assertEquals(200L, created.getPharmacistOperatorId());

        CurrentUserHolder.set(new CurrentUser(100L, "王医生", RoleType.DOCTOR));
        PrescriptionDoctorApprovalRequest doctorApprovalRequest = new PrescriptionDoctorApprovalRequest();
        doctorApprovalRequest.setAction("APPROVE");
        doctorApprovalRequest.setDoctorId(100L);
        doctorApprovalRequest.setDoctorName("王医生");
        prescriptionService.doctorApprove(prescriptionId, doctorApprovalRequest);

        PrescriptionVO approvedByDoctor = prescriptionService.getPrescriptionById(prescriptionId);
        assertEquals(PrescriptionStatus.SUBMITTED.name(), approvedByDoctor.getStatus());
        assertNotNull(approvedByDoctor.getDoctorApprovedAt());

        CurrentUserHolder.set(new CurrentUser(201L, "审核药师李", RoleType.PHARMACIST));
        PrescriptionAuditRequest auditRequest = new PrescriptionAuditRequest();
        auditRequest.setAction("APPROVE");
        auditRequest.setOperatorId(201L);
        auditRequest.setOperatorName("审核药师李");
        prescriptionService.audit(prescriptionId, auditRequest);

        PrescriptionVO audited = prescriptionService.getPrescriptionById(prescriptionId);
        assertEquals(PrescriptionStatus.APPROVED.name(), audited.getStatus());
        assertEquals("审核药师李", audited.getAuditBy());

        CurrentUserHolder.set(new CurrentUser(202L, "发药药师赵", RoleType.PHARMACIST));
        PrescriptionDispenseRequest dispenseRequest = new PrescriptionDispenseRequest();
        dispenseRequest.setOperatorId(202L);
        dispenseRequest.setOperatorName("发药药师赵");
        prescriptionService.dispense(prescriptionId, dispenseRequest);

        PrescriptionVO dispensed = prescriptionService.getPrescriptionById(prescriptionId);
        assertEquals(PrescriptionStatus.DISPENSED.name(), dispensed.getStatus());
        assertEquals("发药药师赵", dispensed.getDispenseBy());
        assertNotNull(dispensed.getDispenseTime());

        InventoryVO inventoryBeforeExpiry = inventoryMapper.findVoById(1L);
        InventoryVO inventoryLaterExpiry = inventoryMapper.findVoById(2L);
        assertEquals(0, inventoryBeforeExpiry.getQuantity());
        assertEquals(8, inventoryLaterExpiry.getQuantity());

        InventoryRecordQueryRequest recordQueryRequest = new InventoryRecordQueryRequest();
        recordQueryRequest.setPageNum(1);
        recordQueryRequest.setPageSize(10);
        recordQueryRequest.setBizNo(dispensed.getPrescriptionNo());
        recordQueryRequest.setRecordType(InventoryRecordType.DISPENSE.name());

        List<InventoryRecordVO> records = inventoryService.queryInventoryRecords(recordQueryRequest).records();
        assertEquals(2, records.size());
        assertEquals(-2, records.get(0).getQuantityChange());
        assertEquals(-10, records.get(1).getQuantityChange());

        PrescriptionQueryRequest queryRequest = new PrescriptionQueryRequest();
        queryRequest.setPageNum(1);
        queryRequest.setPageSize(10);
        queryRequest.setDoctorId(100L);
        queryRequest.setStatus(PrescriptionStatus.DISPENSED.name());
        queryRequest.setPatientName("集成");
        assertEquals(1, prescriptionService.queryPrescriptions(queryRequest).records().size());
    }

    private PrescriptionItemRequest buildItem(Long drugId, Integer quantity) {
        PrescriptionItemRequest item = new PrescriptionItemRequest();
        item.setDrugId(drugId);
        item.setDosage("1片");
        item.setFrequency("tid");
        item.setDays(4);
        item.setQuantity(quantity);
        return item;
    }
}
