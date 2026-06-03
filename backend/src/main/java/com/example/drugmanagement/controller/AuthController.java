package com.example.drugmanagement.controller;

import com.example.drugmanagement.common.auth.CurrentUser;
import com.example.drugmanagement.common.auth.CurrentUserHolder;
import com.example.drugmanagement.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public ApiResponse<CurrentUser> me() {
        return ApiResponse.success(CurrentUserHolder.get());
    }
}
