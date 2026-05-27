package com.sunsetflower.macproxy.localapi.controller;

import com.sunsetflower.macproxy.localapi.service.SystemProxyService;
import com.sunsetflower.macproxy.localapi.service.dto.SystemProxyUpdateRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SystemProxyController {

    private final SystemProxyService systemProxyService;

    public SystemProxyController(SystemProxyService systemProxyService) {
        this.systemProxyService = systemProxyService;
    }

    @GetMapping("/system-proxy")
    public Map<String, Object> getSystemProxyStatus() {
        return Map.of(
                "success", true,
                "data", systemProxyService.getStatus()
        );
    }

    @PutMapping("/system-proxy")
    public Map<String, Object> updateSystemProxyStatus(@RequestBody SystemProxyUpdateRequest request) {
        return Map.of(
                "success", true,
                "data", systemProxyService.update(request.enabled(), request.scope(), request.services())
        );
    }
}
