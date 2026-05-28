package com.sunsetflower.macproxy.localapi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RuntimeControllerIntegrationTest {

    private static Path runtimeRoot;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) throws Exception {
        runtimeRoot = Files.createTempDirectory("runtime-controller-test");
        registry.add("app.runtime.root", () -> runtimeRoot.toString());
        registry.add("app.session.token", () -> "test-session-token");
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void runtimeLogsEndpointReturnsTailLines() throws Exception {
        Path logFile = runtimeRoot.resolve("clash-meta").resolve("logs").resolve("clash-meta.log");
        Files.createDirectories(logFile.getParent());
        Files.writeString(logFile, "line-1\nline-2\nline-3\n");

        mockMvc.perform(get("/api/v1/runtime/logs")
                        .param("limit", "2")
                        .header("Authorization", "Bearer test-session-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.available").value(true))
                .andExpect(jsonPath("$.data.lineCount").value(2))
                .andExpect(jsonPath("$.data.lines[0].lineNumber").value(2))
                .andExpect(jsonPath("$.data.lines[0].content").value("line-2"))
                .andExpect(jsonPath("$.data.lines[1].lineNumber").value(3))
                .andExpect(jsonPath("$.data.lines[1].content").value("line-3"));
    }

    @Test
    void runtimeLogsEndpointReturnsUnavailableWhenLogFileIsMissing() throws Exception {
        mockMvc.perform(get("/api/v1/runtime/logs")
                        .header("Authorization", "Bearer test-session-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.available").value(false))
                .andExpect(jsonPath("$.data.lineCount").value(0))
                .andExpect(jsonPath("$.data.lines").isArray())
                .andExpect(jsonPath("$.data.lines").isEmpty());
    }
}
