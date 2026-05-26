package com.sunsetflower.macproxy.localapi.service;

import com.sunsetflower.macproxy.localapi.service.dto.SessionMetadataResponse;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class SessionService {

    public SessionMetadataResponse getSessionMetadata() {
        return new SessionMetadataResponse(
                "mac-proxy-client",
                "0.1.0",
                "system-proxy",
                OffsetDateTime.now().toString()
        );
    }
}
