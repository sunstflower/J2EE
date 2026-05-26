package com.sunsetflower.macproxy.localapi.controller;

import com.sunsetflower.macproxy.localapi.service.SessionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping("/session")
    public Map<String, Object> session() {
        return Map.of(
                "success", true,
                "data", sessionService.getSessionMetadata()
        );
    }
}
