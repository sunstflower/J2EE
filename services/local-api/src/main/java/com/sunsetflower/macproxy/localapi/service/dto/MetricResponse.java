package com.sunsetflower.macproxy.localapi.service.dto;

public record MetricResponse(
        String label,
        String value,
        String tone
) {
}
