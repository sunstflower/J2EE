package com.example.drugmanagement.common.auth;

import com.example.drugmanagement.common.enums.RoleType;
import com.example.drugmanagement.common.exception.BusinessException;
import com.example.drugmanagement.common.response.ResponseCode;
import com.example.drugmanagement.entity.UserAccount;
import com.example.drugmanagement.mapper.UserAccountMapper;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
public class AuthSessionService {

    private final UserAccountMapper userAccountMapper;
    private final Map<String, CurrentUser> sessions = new ConcurrentHashMap<>();

    public AuthSessionService(UserAccountMapper userAccountMapper) {
        this.userAccountMapper = userAccountMapper;
    }

    public String login(Long userId, String password) {
        UserAccount account = userAccountMapper.findByUserId(userId);
        if (account == null
                || account.getEnabled() == null
                || account.getEnabled() != 1
                || !account.getPassword().equals(password)) {
            throw new BusinessException(ResponseCode.UNAUTHORIZED.getCode(), "用户号或密码错误");
        }

        String token = UUID.randomUUID().toString();
        sessions.put(token, new CurrentUser(
                account.getUserId(),
                account.getUserName(),
                RoleType.valueOf(account.getRole())
        ));
        return token;
    }

    public CurrentUser register(Long userId, String userName, String password) {
        RoleType roleType = inferRole(userId);
        if (userName == null || userName.isBlank() || password == null || password.isBlank()) {
            throw new BusinessException(ResponseCode.VALIDATION_ERROR.getCode(), "用户号、用户名和密码不能为空");
        }
        if (userAccountMapper.findByUserId(userId) != null) {
            throw new BusinessException(ResponseCode.BUSINESS_RULE_VIOLATION.getCode(), "该用户号已存在");
        }

        UserAccount userAccount = new UserAccount();
        userAccount.setUserId(userId);
        userAccount.setUserName(userName);
        userAccount.setRole(roleType.name());
        userAccount.setPassword(password);
        userAccount.setEnabled(1);
        userAccount.setCreatedBy(userName);
        userAccount.setUpdatedBy(userName);
        userAccount.setDeleted(0);
        userAccountMapper.insert(userAccount);

        return new CurrentUser(userAccount.getUserId(), userAccount.getUserName(), roleType);
    }

    public CurrentUser getCurrentUser(String token) {
        CurrentUser currentUser = sessions.get(token);
        if (currentUser == null) {
            throw BusinessException.of(ResponseCode.UNAUTHORIZED);
        }
        return currentUser;
    }

    private RoleType inferRole(Long userId) {
        String rawUserId = String.valueOf(userId);
        if (rawUserId.startsWith("1")) {
            return RoleType.PHARMACIST;
        }
        if (rawUserId.startsWith("2")) {
            return RoleType.DOCTOR;
        }
        throw new BusinessException(ResponseCode.VALIDATION_ERROR.getCode(), "用户号必须以 1 或 2 开头");
    }
}
