package com.example.drugmanagement.service;

import com.example.drugmanagement.common.response.PageResponse;
import com.example.drugmanagement.dto.warning.ExpiryWarningQueryRequest;
import com.example.drugmanagement.vo.warning.ExpiryWarningVO;
import com.example.drugmanagement.vo.warning.LowStockWarningVO;

public interface WarningService {

    PageResponse<LowStockWarningVO> queryLowStockWarnings(int pageNum, int pageSize);

    PageResponse<ExpiryWarningVO> queryExpiryWarnings(ExpiryWarningQueryRequest request);
}
