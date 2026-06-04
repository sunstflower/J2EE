package com.example.drugmanagement.controller;

import com.example.drugmanagement.common.auth.AuthSessionService;
import com.example.drugmanagement.common.response.PageResponse;
import com.example.drugmanagement.mapper.DrugMapper;
import com.example.drugmanagement.mapper.InventoryMapper;
import com.example.drugmanagement.mapper.InventoryRecordMapper;
import com.example.drugmanagement.mapper.PrescriptionItemMapper;
import com.example.drugmanagement.mapper.PrescriptionMapper;
import com.example.drugmanagement.mapper.UserAccountMapper;
import com.example.drugmanagement.mapper.WarningMapper;
import com.example.drugmanagement.service.PrescriptionService;
import com.example.drugmanagement.vo.prescription.PrescriptionVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PrescriptionController.class)
class PrescriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PrescriptionService prescriptionService;

    @MockBean
    private DrugMapper drugMapper;

    @MockBean
    private InventoryMapper inventoryMapper;

    @MockBean
    private InventoryRecordMapper inventoryRecordMapper;

    @MockBean
    private WarningMapper warningMapper;

    @MockBean
    private PrescriptionMapper prescriptionMapper;

    @MockBean
    private PrescriptionItemMapper prescriptionItemMapper;

    @MockBean
    private UserAccountMapper userAccountMapper;

    @MockBean
    private AuthSessionService authSessionService;

    @Test
    void shouldCreateDoctorPrescription() throws Exception {
        when(prescriptionService.createPrescription(any())).thenReturn(1L);

        mockMvc.perform(post("/api/prescriptions")
                        .header("X-User-Id", "100")
                        .header("X-User-Name", "医生王")
                        .header("X-User-Role", "DOCTOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "patientName": "张三",
                                  "createdByRole": "DOCTOR",
                                  "createdByUserId": 100,
                                  "createdByName": "医生王",
                                  "doctorId": 100,
                                  "doctorName": "医生王",
                                  "items": [
                                    {
                                      "drugId": 1,
                                      "dosage": "1片",
                                      "frequency": "bid",
                                      "days": 3,
                                      "quantity": 10
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void shouldReturnPrescriptionPage() throws Exception {
        PrescriptionVO vo = new PrescriptionVO();
        vo.setId(1L);
        vo.setStatus("SUBMITTED");
        when(prescriptionService.queryPrescriptions(any())).thenReturn(PageResponse.of(List.of(vo), 1, 1, 10));

        mockMvc.perform(get("/api/prescriptions")
                        .header("X-User-Id", "200")
                        .header("X-User-Name", "药师李")
                        .header("X-User-Role", "PHARMACIST")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].status").value("SUBMITTED"));
    }

    @Test
    void shouldDoctorApprovePrescription() throws Exception {
        doNothing().when(prescriptionService).doctorApprove(any(), any());

        mockMvc.perform(post("/api/prescriptions/1/doctor-approve")
                        .header("X-User-Id", "100")
                        .header("X-User-Name", "医生王")
                        .header("X-User-Role", "DOCTOR")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "action": "APPROVE",
                                  "doctorName": "医生王",
                                  "doctorId": 100
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void shouldAuditPrescription() throws Exception {
        doNothing().when(prescriptionService).audit(any(), any());

        mockMvc.perform(post("/api/prescriptions/1/audit")
                        .header("X-User-Id", "300")
                        .header("X-User-Name", "药师李")
                        .header("X-User-Role", "PHARMACIST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "action": "APPROVE",
                                  "operatorName": "药师李",
                                  "operatorId": 300
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void shouldDispensePrescription() throws Exception {
        doNothing().when(prescriptionService).dispense(any(), any());

        mockMvc.perform(post("/api/prescriptions/1/dispense")
                        .header("X-User-Id", "300")
                        .header("X-User-Name", "药师李")
                        .header("X-User-Role", "PHARMACIST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "operatorName": "药师李",
                                  "operatorId": 300
                                }
                                """))
                .andExpect(status().isOk());
    }
}
