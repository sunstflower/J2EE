package com.sunsetflower.macproxy.localapi.controller;

import com.sunsetflower.macproxy.localapi.service.RuntimeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class RuntimeController {

    private final RuntimeService runtimeService;

    public RuntimeController(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @GetMapping("/runtime")
    public Map<String, Object> getRuntime() {
        return Map.of(
                "success", true,
                "data", runtimeService.getRuntimeSummary()
        );
    }
}
