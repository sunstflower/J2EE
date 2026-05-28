package com.sunsetflower.macproxy.localapi.service.dto;

public record RuntimeLogLineResponse(
        int lineNumber,
        String content
) {
}
