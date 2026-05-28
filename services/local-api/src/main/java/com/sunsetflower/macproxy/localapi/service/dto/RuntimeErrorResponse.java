package com.sunsetflower.macproxy.localapi.service.dto;

public record RuntimeErrorResponse(
        String source,
        String severity,
        String message
) {
}
