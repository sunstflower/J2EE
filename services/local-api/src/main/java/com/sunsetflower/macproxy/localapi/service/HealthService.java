package com.sunsetflower.macproxy.localapi.service;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class HealthService {

    public Map<String, Object> summary() {
        return Map.of(
                "application", "UP",
                "coreManager", "NOT_INITIALIZED",
                "database", "NOT_INITIALIZED"
        );
    }
}
