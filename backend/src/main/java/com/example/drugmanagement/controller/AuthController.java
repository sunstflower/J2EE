package com.example.drugmanagement.controller;

import com.example.drugmanagement.common.auth.CurrentUser;
import com.example.drugmanagement.common.auth.CurrentUserHolder;
import com.example.drugmanagement.common.auth.AuthSessionService;
import com.example.drugmanagement.common.response.ApiResponse;
import com.example.drugmanagement.dto.auth.LoginRequest;
import com.example.drugmanagement.dto.auth.RegisterRequest;
import com.example.drugmanagement.vo.auth.LoginVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthSessionService authSessionService;

    public AuthController(AuthSessionService authSessionService) {
        this.authSessionService = authSessionService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@RequestBody LoginRequest request) {
        String token = authSessionService.login(request.getUserId(), request.getPassword());
        CurrentUser currentUser = authSessionService.getCurrentUser(token);
        return ApiResponse.success(new LoginVO(token, currentUser));
    }

    @PostMapping("/register")
    public ApiResponse<CurrentUser> register(@RequestBody RegisterRequest request) {
        return ApiResponse.success(
                authSessionService.register(request.getUserId(), request.getUserName(), request.getPassword())
        );
    }

    @GetMapping("/me")
    public ApiResponse<CurrentUser> me() {
        return ApiResponse.success(CurrentUserHolder.get());
    }
}
