package com.example.drugmanagement.controller;

import com.example.drugmanagement.mapper.DrugMapper;
import com.example.drugmanagement.mapper.InventoryMapper;
import com.example.drugmanagement.mapper.InventoryRecordMapper;
import com.example.drugmanagement.mapper.PrescriptionItemMapper;
import com.example.drugmanagement.mapper.PrescriptionMapper;
import com.example.drugmanagement.mapper.WarningMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    void shouldReturnCurrentUser() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("X-User-Id", "100")
                        .header("X-User-Name", "王医生")
                        .header("X-User-Role", "DOCTOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(100))
                .andExpect(jsonPath("$.data.userName").value("王医生"))
                .andExpect(jsonPath("$.data.role").value("DOCTOR"));
    }

    @Test
    void shouldRejectWhenHeadersMissing() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(4010));
    }
}
