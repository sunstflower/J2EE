package com.example.drugmanagement.common.exception;

import com.example.drugmanagement.common.response.ResponseCode;

public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static BusinessException of(ResponseCode responseCode) {
        return new BusinessException(responseCode.getCode(), responseCode.getMessage());
    }
}
