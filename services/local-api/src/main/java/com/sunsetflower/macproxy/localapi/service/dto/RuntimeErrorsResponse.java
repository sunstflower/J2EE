package com.sunsetflower.macproxy.localapi.service.dto;

import java.util.List;

public record RuntimeErrorsResponse(
        int errorCount,
        List<RuntimeErrorResponse> errors
) {
}
