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

    private static final Map<Long, DemoUserCredential> DEMO_USERS = Map.of(
            1001L, new DemoUserCredential(1001L, "张药师", RoleType.PHARMACIST, "pharm123"),
            2001L, new DemoUserCredential(2001L, "王医生", RoleType.DOCTOR, "doctor123")
    );

    private final Map<String, CurrentUser> sessions = new ConcurrentHashMap<>();

    public String login(Long userId, String password) {
        DemoUserCredential credential = DEMO_USERS.get(userId);
        if (credential == null || !credential.password().equals(password)) {
            throw new BusinessException(ResponseCode.UNAUTHORIZED.getCode(), "用户号或密码错误");
        }

        String token = UUID.randomUUID().toString();
        sessions.put(token, new CurrentUser(
                credential.userId(),
                credential.userName(),
                credential.role()
        ));
        return token;
    }

    public CurrentUser getCurrentUser(String token) {
        CurrentUser currentUser = sessions.get(token);
        if (currentUser == null) {
            throw BusinessException.of(ResponseCode.UNAUTHORIZED);
        }
        return currentUser;
    }

    private record DemoUserCredential(Long userId, String userName, RoleType role, String password) {
    }
}
