package com.example.drugmanagement.service.impl;

import com.example.drugmanagement.common.response.PageResponse;
import com.example.drugmanagement.dto.warning.ExpiryWarningQueryRequest;
import com.example.drugmanagement.mapper.WarningMapper;
import com.example.drugmanagement.service.WarningService;
import com.example.drugmanagement.vo.warning.ExpiryWarningVO;
import com.example.drugmanagement.vo.warning.LowStockWarningVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarningServiceImpl implements WarningService {

    private final WarningMapper warningMapper;

    public WarningServiceImpl(WarningMapper warningMapper) {
        this.warningMapper = warningMapper;
    }

    @Override
    public PageResponse<LowStockWarningVO> queryLowStockWarnings(int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<LowStockWarningVO> records = warningMapper.findLowStockWarnings(pageSize, offset);
        long total = warningMapper.countLowStockWarnings();
        return PageResponse.of(records, total, pageNum, pageSize);
    }

    @Override
    public PageResponse<ExpiryWarningVO> queryExpiryWarnings(ExpiryWarningQueryRequest request) {
        List<ExpiryWarningVO> records = warningMapper.findExpiryWarnings(request);
        long total = warningMapper.countExpiryWarnings(request);
        return PageResponse.of(records, total, request.getPageNum(), request.getPageSize());
    }
}
