package com.example.drugmanagement.common.auth;

import com.example.drugmanagement.common.enums.RoleType;

public record CurrentUser(Long userId, String userName, RoleType role) {
}
