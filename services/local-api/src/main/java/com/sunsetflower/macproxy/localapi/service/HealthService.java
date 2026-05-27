package com.sunsetflower.macproxy.localapi.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class HealthService {

    private final JdbcTemplate jdbcTemplate;
    private final CoreManagerService coreManagerService;

    public HealthService(JdbcTemplate jdbcTemplate, CoreManagerService coreManagerService) {
        this.jdbcTemplate = jdbcTemplate;
        this.coreManagerService = coreManagerService;
    }

    public Map<String, Object> summary() {
        String databaseStatus = resolveDatabaseStatus();
        String coreManagerStatus = coreManagerService.getStatus().state();

        return Map.of(
                "application", "UP",
                "coreManager", coreManagerStatus,
                "database", databaseStatus
        );
    }

    private String resolveDatabaseStatus() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return "UP";
        } catch (RuntimeException error) {
            return "DOWN";
        }
    }
}
