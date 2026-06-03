package com.example.drugmanagement.common.response;

public enum ResponseCode {
    SUCCESS(0, "success"),
    BAD_REQUEST(400, "bad request"),
    VALIDATION_ERROR(4001, "validation error"),
    UNAUTHORIZED(4010, "unauthorized"),
    RESOURCE_NOT_FOUND(4040, "resource not found"),
    BUSINESS_RULE_VIOLATION(4002, "business rule violation"),
    INTERNAL_SERVER_ERROR(5000, "internal server error");

    private final int code;
    private final String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
