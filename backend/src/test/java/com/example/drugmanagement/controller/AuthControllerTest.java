package com.example.drugmanagement.controller;

import com.example.drugmanagement.common.auth.AuthSessionService;
import com.example.drugmanagement.common.auth.CurrentUser;
import com.example.drugmanagement.common.enums.RoleType;
import com.example.drugmanagement.mapper.DrugMapper;
import com.example.drugmanagement.mapper.InventoryMapper;
import com.example.drugmanagement.mapper.InventoryRecordMapper;
import com.example.drugmanagement.mapper.PrescriptionItemMapper;
import com.example.drugmanagement.mapper.PrescriptionMapper;
import com.example.drugmanagement.mapper.UserAccountMapper;
import com.example.drugmanagement.mapper.WarningMapper;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @MockBean
    private UserAccountMapper userAccountMapper;

    @MockBean
    private AuthSessionService authSessionService;

    @Test
    void shouldLoginWithDemoUser() throws Exception {
        given(authSessionService.login(1001L, "pharm123")).willReturn("demo-token");
        given(authSessionService.getCurrentUser("demo-token"))
                .willReturn(new CurrentUser(1001L, "张药师", RoleType.PHARMACIST));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1001,
                                  "password": "pharm123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.userId").value(1001))
                .andExpect(jsonPath("$.data.user.role").value("PHARMACIST"));
    }

    @Test
    void shouldRegisterUser() throws Exception {
        given(authSessionService.register(1008L, "李药师", "abc123"))
                .willReturn(new CurrentUser(1008L, "李药师", RoleType.PHARMACIST));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1008,
                                  "userName": "李药师",
                                  "password": "abc123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(1008))
                .andExpect(jsonPath("$.data.role").value("PHARMACIST"));
    }

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
