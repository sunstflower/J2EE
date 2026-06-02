package com.example.drugmanagement.common.response;

public record ApiResponse<T>(int code, String message, T data) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResponseCode.SUCCESS.getCode(), ResponseCode.SUCCESS.getMessage(), data);
    }

    public static ApiResponse<Void> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> failure(ResponseCode responseCode) {
        return new ApiResponse<>(responseCode.getCode(), responseCode.getMessage(), null);
    }

    public static <T> ApiResponse<T> failure(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
