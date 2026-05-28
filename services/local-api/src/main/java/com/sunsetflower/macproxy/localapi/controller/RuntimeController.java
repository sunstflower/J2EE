package com.sunsetflower.macproxy.localapi.controller;

import com.sunsetflower.macproxy.localapi.service.RuntimeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/runtime/logs")
    public Map<String, Object> getRuntimeLogs(@RequestParam(required = false) Integer limit) {
        return Map.of(
                "success", true,
                "data", runtimeService.getRuntimeLogs(limit)
        );
    }

    @GetMapping("/runtime/errors")
    public Map<String, Object> getRuntimeErrors() {
        return Map.of(
                "success", true,
                "data", runtimeService.getRuntimeErrors()
        );
    }
}
