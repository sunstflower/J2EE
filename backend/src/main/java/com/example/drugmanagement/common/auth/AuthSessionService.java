package com.example.drugmanagement.common.auth;

import com.example.drugmanagement.common.enums.RoleType;
import com.example.drugmanagement.common.exception.BusinessException;
import com.example.drugmanagement.common.response.ResponseCode;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthSessionService {

    private final Map<String, CurrentUser> sessions = new ConcurrentHashMap<>();

    public String createSession(Long userId, String userName, RoleType roleType) {
        String token = UUID.randomUUID().toString();
        sessions.put(token, new CurrentUser(userId, userName, roleType));
        return token;
    }

    public CurrentUser getCurrentUser(String token) {
        CurrentUser currentUser = sessions.get(token);
        if (currentUser == null) {
            throw BusinessException.of(ResponseCode.UNAUTHORIZED);
        }
        return currentUser;
    }
}
