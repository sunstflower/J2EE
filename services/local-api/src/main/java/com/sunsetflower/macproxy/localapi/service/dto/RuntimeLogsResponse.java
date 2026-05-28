package com.sunsetflower.macproxy.localapi.service.dto;

import java.util.List;

public record RuntimeLogsResponse(
        String logFile,
        boolean available,
        int lineCount,
        List<RuntimeLogLineResponse> lines
) {
}
