package com.example.drugmanagement.common.auth;

import com.example.drugmanagement.common.enums.RoleType;
import com.example.drugmanagement.common.exception.BusinessException;
import com.example.drugmanagement.common.response.ResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_NAME = "X-User-Name";
    private static final String HEADER_USER_ROLE = "X-User-Role";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthSessionService authSessionService;

    public AuthInterceptor(AuthSessionService authSessionService) {
        this.authSessionService = authSessionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader(HEADER_AUTHORIZATION);
        if (authorization != null && authorization.startsWith(BEARER_PREFIX)) {
            CurrentUserHolder.set(authSessionService.getCurrentUser(authorization.substring(BEARER_PREFIX.length())));
            return true;
        }

        String userIdHeader = request.getHeader(HEADER_USER_ID);
        String userName = request.getHeader(HEADER_USER_NAME);
        String userRole = request.getHeader(HEADER_USER_ROLE);

        if (userIdHeader == null || userIdHeader.isBlank()
                || userName == null || userName.isBlank()
                || userRole == null || userRole.isBlank()) {
            throw BusinessException.of(ResponseCode.UNAUTHORIZED);
        }

        try {
            Long userId = Long.valueOf(userIdHeader);
            RoleType roleType = RoleType.valueOf(userRole);
            CurrentUserHolder.set(new CurrentUser(userId, decodeHeader(userName), roleType));
            return true;
        } catch (IllegalArgumentException exception) {
            throw BusinessException.of(ResponseCode.UNAUTHORIZED);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        CurrentUserHolder.clear();
    }

    private String decodeHeader(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
