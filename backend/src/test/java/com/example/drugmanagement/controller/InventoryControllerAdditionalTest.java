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
import com.example.drugmanagement.service.InventoryService;
import com.example.drugmanagement.vo.inventory.InventoryRecordVO;
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

@WebMvcTest(InventoryController.class)
class InventoryControllerAdditionalTest {

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
    void shouldOutboundInventory() throws Exception {
        doNothing().when(inventoryService).outbound(any());

        mockMvc.perform(post("/api/inventories/outbound")
                        .header("X-User-Id", "200")
                        .header("X-User-Name", "张药师")
                        .header("X-User-Role", "PHARMACIST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "drugId": 1,
                                  "quantity": 5,
                                  "bizNo": "OUT-20260603-001",
                                  "operatorName": "药师李四",
                                  "remark": "窗口发药"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void shouldCheckInventory() throws Exception {
        doNothing().when(inventoryService).check(any());

        mockMvc.perform(post("/api/inventories/check")
                        .header("X-User-Id", "200")
                        .header("X-User-Name", "张药师")
                        .header("X-User-Role", "PHARMACIST")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inventoryId": 1,
                                  "actualQuantity": 20,
                                  "bizNo": "CHK-20260603-001",
                                  "operatorName": "药师李四",
                                  "remark": "月度盘点"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    @Test
    void shouldReturnInventoryRecordPage() throws Exception {
        InventoryRecordVO recordVO = new InventoryRecordVO();
        recordVO.setId(1L);
        recordVO.setRecordType("INBOUND");
        when(inventoryService.queryInventoryRecords(any())).thenReturn(PageResponse.of(List.of(recordVO), 1, 1, 10));

        mockMvc.perform(get("/api/inventories/records")
                        .header("X-User-Id", "200")
                        .header("X-User-Name", "张药师")
                        .header("X-User-Role", "PHARMACIST")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].recordType").value("INBOUND"));
    }
}
