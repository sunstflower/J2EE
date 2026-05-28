package com.sunsetflower.macproxy.localapi.web;

import com.sunsetflower.macproxy.localapi.config.AppSessionProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SessionTokenInterceptor implements HandlerInterceptor {

    private final AppSessionProperties sessionProperties;

    public SessionTokenInterceptor(AppSessionProperties sessionProperties) {
        this.sessionProperties = sessionProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        String configuredToken = sessionProperties.getToken();

        if (configuredToken == null || configuredToken.isBlank()) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Session token is not configured");
            return false;
        }

        String authorization = request.getHeader("Authorization");
        String expected = "Bearer " + configuredToken;

        if (!expected.equals(authorization)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid session token");
            return false;
        }

        return true;
    }
}
