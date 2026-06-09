package com.example.drugmanagement.controller;

import com.example.drugmanagement.common.auth.AuthSessionService;
import com.example.drugmanagement.common.enums.RoleType;
import com.example.drugmanagement.common.exception.BusinessException;
import com.example.drugmanagement.common.response.ApiResponse;
import com.example.drugmanagement.common.response.ResponseCode;
import com.example.drugmanagement.dto.auth.LoginRequest;
import com.example.drugmanagement.entity.UserAccount;
import com.example.drugmanagement.mapper.UserAccountMapper;
import com.example.drugmanagement.vo.auth.LoginVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthSessionService authSessionService;
    private final UserAccountMapper userAccountMapper;

    public AuthController(AuthSessionService authSessionService, UserAccountMapper userAccountMapper) {
        this.authSessionService = authSessionService;
        this.userAccountMapper = userAccountMapper;
    }

    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        DemoUser demoUser = resolveDemoUser(request.getUserId(), request.getPassword());
        String token = authSessionService.createSession(demoUser.userId(), demoUser.userName(), demoUser.roleType());
        return ApiResponse.success(new LoginVO(token, demoUser.toCurrentUser()));
    }

    private DemoUser resolveDemoUser(Long userId, String password) {
        UserAccount userAccount = userAccountMapper.findByUserId(userId);
        if (userAccount != null
                && Integer.valueOf(1).equals(userAccount.getEnabled())
                && password != null
                && password.equals(userAccount.getPassword())) {
            return new DemoUser(
                    userAccount.getUserId(),
                    userAccount.getUserName(),
                    RoleType.valueOf(userAccount.getRole())
            );
        }

        if (Long.valueOf(1001L).equals(userId) && "pharm123".equals(password)) {
            return new DemoUser(1001L, "张药师", RoleType.PHARMACIST);
        }
        if (Long.valueOf(2001L).equals(userId) && "doctor123".equals(password)) {
            return new DemoUser(2001L, "王医生", RoleType.DOCTOR);
        }
        throw BusinessException.of(ResponseCode.UNAUTHORIZED);
    }

    private record DemoUser(Long userId, String userName, RoleType roleType) {
        private com.example.drugmanagement.common.auth.CurrentUser toCurrentUser() {
            return new com.example.drugmanagement.common.auth.CurrentUser(userId, userName, roleType);
        }
    }
}
