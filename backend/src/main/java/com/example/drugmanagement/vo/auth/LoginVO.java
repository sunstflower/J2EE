package com.example.drugmanagement.vo.auth;

import com.example.drugmanagement.common.auth.CurrentUser;

public class LoginVO {

    private String token;
    private CurrentUser user;

    public LoginVO(String token, CurrentUser user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public CurrentUser getUser() {
        return user;
    }
}
