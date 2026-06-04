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
import com.example.drugmanagement.service.WarningService;
import com.example.drugmanagement.vo.warning.ExpiryWarningVO;
import com.example.drugmanagement.vo.warning.LowStockWarningVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WarningController.class)
class WarningControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WarningService warningService;

    @MockBean
    private WarningMapper warningMapper;

    @MockBean
    private DrugMapper drugMapper;

    @MockBean
    private InventoryMapper inventoryMapper;

    @MockBean
    private InventoryRecordMapper inventoryRecordMapper;

    @MockBean
    private PrescriptionMapper prescriptionMapper;

    @MockBean
    private PrescriptionItemMapper prescriptionItemMapper;

    @MockBean
    private UserAccountMapper userAccountMapper;

    @MockBean
    private AuthSessionService authSessionService;

    @Test
    void shouldReturnLowStockWarnings() throws Exception {
        LowStockWarningVO warningVO = new LowStockWarningVO();
        warningVO.setDrugCode("DRUG-001");
        when(warningService.queryLowStockWarnings(1, 10)).thenReturn(PageResponse.of(List.of(warningVO), 1, 1, 10));

        mockMvc.perform(get("/api/warnings/low-stock")
                        .header("X-User-Id", "200")
                        .header("X-User-Name", "张药师")
                        .header("X-User-Role", "PHARMACIST")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].drugCode").value("DRUG-001"));
    }

    @Test
    void shouldReturnExpiryWarnings() throws Exception {
        ExpiryWarningVO warningVO = new ExpiryWarningVO();
        warningVO.setWarningType("EXPIRY");
        when(warningService.queryExpiryWarnings(any())).thenReturn(PageResponse.of(List.of(warningVO), 1, 1, 10));

        mockMvc.perform(get("/api/warnings/expiry")
                        .header("X-User-Id", "200")
                        .header("X-User-Name", "张药师")
                        .header("X-User-Role", "PHARMACIST")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("expiryDays", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[0].warningType").value("EXPIRY"));
    }

    @Test
    void shouldRejectExpiryWarningsWhenExpiryDaysInvalid() throws Exception {
        mockMvc.perform(get("/api/warnings/expiry")
                        .header("X-User-Id", "200")
                        .header("X-User-Name", "张药师")
                        .header("X-User-Role", "PHARMACIST")
                        .param("pageNum", "1")
                        .param("pageSize", "10")
                        .param("expiryDays", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4001));
    }
}
