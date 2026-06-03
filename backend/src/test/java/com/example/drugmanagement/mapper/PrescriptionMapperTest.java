package com.example.drugmanagement.mapper;

import com.example.drugmanagement.common.enums.DoctorApprovalStatus;
import com.example.drugmanagement.common.enums.PrescriptionStatus;
import com.example.drugmanagement.dto.prescription.PrescriptionQueryRequest;
import com.example.drugmanagement.entity.Prescription;
import com.example.drugmanagement.entity.PrescriptionItem;
import com.example.drugmanagement.vo.prescription.PrescriptionItemVO;
import com.example.drugmanagement.vo.prescription.PrescriptionVO;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:prescription_mapper;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver"
})
@Sql(scripts = "/h2/prescription-mapper-init.sql")
class PrescriptionMapperTest {

    @Autowired
    private PrescriptionMapper prescriptionMapper;

    @Autowired
    private PrescriptionItemMapper prescriptionItemMapper;

    @Test
    void shouldQueryPrescriptionPageAndDetail() {
        long prescriptionId = insertPrescription("RX-MAPPER-001", "SUBMITTED", 100L, "王医生");
        insertPrescriptionItem(prescriptionId, 1L, 5);

        PrescriptionQueryRequest request = new PrescriptionQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        request.setStatus("SUBMITTED");
        request.setDoctorId(100L);
        request.setPatientName("张");

        List<PrescriptionVO> records = prescriptionMapper.findPage(request);
        long total = prescriptionMapper.count(request);
        PrescriptionVO detail = prescriptionMapper.findById(prescriptionId);
        List<PrescriptionItemVO> items = prescriptionItemMapper.findByPrescriptionId(prescriptionId);

        assertEquals(1, total);
        assertEquals(1, records.size());
        assertEquals("RX-MAPPER-001", records.get(0).getPrescriptionNo());
        assertNotNull(detail);
        assertEquals("张三", detail.getPatientName());
        assertEquals(1, items.size());
        assertEquals("DRUG-001", items.get(0).getDrugCode());
    }

    @Test
    void shouldUpdateStatusByCurrentStatus() {
        long prescriptionId = insertPrescription("RX-MAPPER-002", "APPROVED", 101L, "李医生");

        int updatedRows = prescriptionMapper.updateStatusByCurrentStatus(
                prescriptionId,
                PrescriptionStatus.APPROVED.name(),
                PrescriptionStatus.DISPENSED.name(),
                DoctorApprovalStatus.APPROVED.name(),
                LocalDateTime.of(2026, 6, 3, 10, 0),
                "药师甲",
                LocalDateTime.of(2026, 6, 3, 10, 10),
                "药师乙",
                LocalDateTime.of(2026, 6, 3, 10, 20),
                null,
                "药师乙"
        );

        Prescription prescription = prescriptionMapper.findEntityById(prescriptionId);

        assertEquals(1, updatedRows);
        assertEquals(PrescriptionStatus.DISPENSED.name(), prescription.getStatus());
        assertEquals("药师乙", prescription.getDispenseBy());
        assertNotNull(prescription.getDispenseTime());
    }

    private long insertPrescription(String prescriptionNo, String status, Long doctorId, String doctorName) {
        Prescription prescription = new Prescription();
        prescription.setPrescriptionNo(prescriptionNo);
        prescription.setPatientName("张三");
        prescription.setCreatedByUserId(doctorId);
        prescription.setCreatedByRole("DOCTOR");
        prescription.setDoctorId(doctorId);
        prescription.setDoctorName(doctorName);
        prescription.setStatus(status);
        prescription.setDoctorApprovalStatus(DoctorApprovalStatus.NONE.name());
        prescription.setCreatedBy(doctorName);
        prescription.setUpdatedBy(doctorName);
        prescription.setDeleted(0);
        prescriptionMapper.insert(prescription);
        return prescription.getId();
    }

    private void insertPrescriptionItem(long prescriptionId, long drugId, int quantity) {
        PrescriptionItem item = new PrescriptionItem();
        item.setPrescriptionId(prescriptionId);
        item.setDrugId(drugId);
        item.setDosage("1片");
        item.setFrequency("bid");
        item.setDays(3);
        item.setQuantity(quantity);
        item.setCreatedBy("seed");
        item.setUpdatedBy("seed");
        item.setDeleted(0);
        prescriptionItemMapper.insertBatch(List.of(item));
    }
}
