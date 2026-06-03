package com.example.drugmanagement.controller;

import com.example.drugmanagement.common.response.PageResponse;
import com.example.drugmanagement.mapper.DrugMapper;
import com.example.drugmanagement.mapper.InventoryMapper;
import com.example.drugmanagement.mapper.InventoryRecordMapper;
import com.example.drugmanagement.service.InventoryService;
import com.example.drugmanagement.vo.inventory.InventoryVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InventoryService inventoryService;

    @MockBean
    private DrugMapper drugMapper;

    @MockBean
    private InventoryMapper inventoryMapper;

    @MockBean
    private InventoryRecordMapper inventoryRecordMapper;

    @Test
    void shouldInboundInventory() throws Exception {
        when(inventoryService.inbound(any())).thenReturn(10L);

        mockMvc.perform(post("/api/inventories/inbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "drugId": 1,
                                  "batchNo": "BATCH-001",
                                  "expiryDate": "2027-01-01",
                                  "quantity": 50,
                                  "locationCode": "A-01",
                                  "bizNo": "IN-20260603-001",
                                  "operatorName": "药师张三",
                                  "remark": "首批入库"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(10));
    }

    @Test
    void shouldRejectInboundWhenQuantityMissing() throws Exception {
        mockMvc.perform(post("/api/inventories/inbound")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "drugId": 1,
                                  "batchNo": "BATCH-001",
                                  "expiryDate": "2027-01-01",
                                  "bizNo": "IN-20260603-001",
                                  "operatorName": "药师张三"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }

    @Test
    void shouldReturnInventoryPage() throws Exception {
        InventoryVO inventoryVO = new InventoryVO();
        inventoryVO.setId(10L);
        inventoryVO.setDrugCode("DRUG-100");
        when(inventoryService.queryInventories(any())).thenReturn(PageResponse.of(List.of(inventoryVO), 1, 1, 10));

        mockMvc.perform(get("/api/inventories")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].drugCode").value("DRUG-100"));
    }

    @Test
    void shouldReturnInventoryDetail() throws Exception {
        InventoryVO inventoryVO = new InventoryVO();
        inventoryVO.setId(10L);
        inventoryVO.setBatchNo("BATCH-001");
        when(inventoryService.getInventoryById(10L)).thenReturn(inventoryVO);

        mockMvc.perform(get("/api/inventories/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.batchNo").value("BATCH-001"));
    }
}
