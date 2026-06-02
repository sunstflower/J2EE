package com.example.drugmanagement.common;

import com.example.drugmanagement.common.response.ApiResponse;
import com.example.drugmanagement.common.response.ResponseCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ApiResponseTest {

    @Test
    void shouldCreateSuccessResponse() {
        ApiResponse<String> response = ApiResponse.success("ok");

        assertEquals(0, response.code());
        assertEquals("success", response.message());
        assertEquals("ok", response.data());
    }

    @Test
    void shouldCreateFailureResponseFromResponseCode() {
        ApiResponse<Void> response = ApiResponse.failure(ResponseCode.BUSINESS_RULE_VIOLATION);

        assertEquals(4002, response.code());
        assertEquals("business rule violation", response.message());
        assertNull(response.data());
    }
}
