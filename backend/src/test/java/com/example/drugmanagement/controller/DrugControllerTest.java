package com.example.drugmanagement.controller;

import com.example.drugmanagement.mapper.DrugMapper;
import com.example.drugmanagement.mapper.InventoryMapper;
import com.example.drugmanagement.mapper.InventoryRecordMapper;
import com.example.drugmanagement.mapper.PrescriptionItemMapper;
import com.example.drugmanagement.mapper.PrescriptionMapper;
import com.example.drugmanagement.mapper.WarningMapper;
import com.example.drugmanagement.common.response.PageResponse;
import com.example.drugmanagement.service.DrugService;
import com.example.drugmanagement.vo.drug.DrugVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DrugController.class)
class DrugControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DrugService drugService;

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

    @Test
    void shouldReturnDrugPage() throws Exception {
        DrugVO drugVO = buildDrugVO();
        when(drugService.queryDrugs(any())).thenReturn(PageResponse.of(List.of(drugVO), 1, 1, 10));

        mockMvc.perform(get("/api/drugs")
                        .header("X-User-Id", "200")
                        .header("X-User-Name", "张药师")
                        .header("X-User-Role", "PHARMACIST")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.records[0].drugCode").value("DRUG-100"));
    }

    @Test
    void shouldReturnDrugDetail() throws Exception {
        when(drugService.getDrugById(1L)).thenReturn(buildDrugVO());

        mockMvc.perform(get("/api/drugs/1")
                        .header("X-User-Id", "200")
                        .header("X-User-Name", "张药师")
                        .header("X-User-Role", "PHARMACIST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.drugName").value("感冒灵颗粒"));
    }

    @Test
    void shouldCreateDrug() throws Exception {
        when(drugService.createDrug(any())).thenReturn(1L);

        mockMvc.perform(post("/api/drugs")
                        .header("X-User-Id", "200")
                        .header("X-User-Name", "张药师")
                        .header("X-User-Role", "PHARMACIST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "drugCode": "DRUG-100",
                                  "drugName": "感冒灵颗粒",
                                  "genericName": "感冒灵",
                                  "category": "中成药",
                                  "specification": "10g*9袋",
                                  "unit": "盒",
                                  "manufacturer": "示例药厂",
                                  "approvalNumber": "国药准字Z1000001",
                                  "purchasePrice": 10.00,
                                  "salePrice": 15.00,
                                  "lowStockThreshold": 5,
                                  "enabled": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(1));
    }

    @Test
    void shouldRejectCreateDrugWhenDrugCodeMissing() throws Exception {
        mockMvc.perform(post("/api/drugs")
                        .header("X-User-Id", "200")
                        .header("X-User-Name", "张药师")
                        .header("X-User-Role", "PHARMACIST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "drugName": "感冒灵颗粒",
                                  "unit": "盒",
                                  "purchasePrice": 10.00,
                                  "salePrice": 15.00,
                                  "lowStockThreshold": 5,
                                  "enabled": 1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    void shouldUpdateDrug() throws Exception {
        doNothing().when(drugService).updateDrug(eq(1L), any());

        mockMvc.perform(put("/api/drugs/1")
                        .header("X-User-Id", "200")
                        .header("X-User-Name", "张药师")
                        .header("X-User-Role", "PHARMACIST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "drugName": "感冒灵颗粒",
                                  "genericName": "感冒灵",
                                  "category": "中成药",
                                  "specification": "10g*9袋",
                                  "unit": "盒",
                                  "manufacturer": "示例药厂",
                                  "approvalNumber": "国药准字Z1000001",
                                  "purchasePrice": 10.00,
                                  "salePrice": 15.00,
                                  "lowStockThreshold": 5,
                                  "enabled": 1
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void shouldDeleteDrug() throws Exception {
        doNothing().when(drugService).deleteDrug(1L);

        mockMvc.perform(delete("/api/drugs/1")
                        .header("X-User-Id", "200")
                        .header("X-User-Name", "张药师")
                        .header("X-User-Role", "PHARMACIST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private DrugVO buildDrugVO() {
        DrugVO drugVO = new DrugVO();
        drugVO.setId(1L);
        drugVO.setDrugCode("DRUG-100");
        drugVO.setDrugName("感冒灵颗粒");
        drugVO.setGenericName("感冒灵");
        drugVO.setCategory("中成药");
        drugVO.setSpecification("10g*9袋");
        drugVO.setUnit("盒");
        drugVO.setManufacturer("示例药厂");
        drugVO.setApprovalNumber("国药准字Z1000001");
        drugVO.setPurchasePrice(new BigDecimal("10.00"));
        drugVO.setSalePrice(new BigDecimal("15.00"));
        drugVO.setLowStockThreshold(5);
        drugVO.setEnabled(1);
        return drugVO;
    }
}
