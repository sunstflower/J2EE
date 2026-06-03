package com.example.drugmanagement.common.auth;

import com.example.drugmanagement.common.enums.RoleType;
import com.example.drugmanagement.common.exception.BusinessException;
import com.example.drugmanagement.common.response.ResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_NAME = "X-User-Name";
    private static final String HEADER_USER_ROLE = "X-User-Role";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
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
            CurrentUserHolder.set(new CurrentUser(userId, userName, roleType));
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
}
