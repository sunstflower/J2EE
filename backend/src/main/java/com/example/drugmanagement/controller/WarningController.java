package com.example.drugmanagement.controller;

import com.example.drugmanagement.common.response.ApiResponse;
import com.example.drugmanagement.common.response.PageResponse;
import com.example.drugmanagement.dto.warning.ExpiryWarningQueryRequest;
import com.example.drugmanagement.service.WarningService;
import com.example.drugmanagement.vo.warning.ExpiryWarningVO;
import com.example.drugmanagement.vo.warning.LowStockWarningVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/warnings")
public class WarningController {

    private final WarningService warningService;

    public WarningController(WarningService warningService) {
        this.warningService = warningService;
    }

    @GetMapping("/low-stock")
    public ApiResponse<PageResponse<LowStockWarningVO>> queryLowStockWarnings(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "must be greater than or equal to 1") int pageNum,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "must be greater than or equal to 1")
            @Max(value = 100, message = "must be less than or equal to 100") int pageSize) {
        return ApiResponse.success(warningService.queryLowStockWarnings(pageNum, pageSize));
    }

    @GetMapping("/expiry")
    public ApiResponse<PageResponse<ExpiryWarningVO>> queryExpiryWarnings(@Valid ExpiryWarningQueryRequest request) {
        return ApiResponse.success(warningService.queryExpiryWarnings(request));
    }
}
