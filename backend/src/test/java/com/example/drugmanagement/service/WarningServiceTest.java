package com.example.drugmanagement.service;

import com.example.drugmanagement.common.response.PageResponse;
import com.example.drugmanagement.dto.warning.ExpiryWarningQueryRequest;
import com.example.drugmanagement.mapper.WarningMapper;
import com.example.drugmanagement.service.impl.WarningServiceImpl;
import com.example.drugmanagement.vo.warning.ExpiryWarningVO;
import com.example.drugmanagement.vo.warning.LowStockWarningVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarningServiceTest {

    @Mock
    private WarningMapper warningMapper;

    @InjectMocks
    private WarningServiceImpl warningService;

    @Test
    void shouldReturnLowStockWarnings() {
        LowStockWarningVO warningVO = new LowStockWarningVO();
        warningVO.setDrugId(1L);
        when(warningMapper.findLowStockWarnings(10, 0)).thenReturn(List.of(warningVO));
        when(warningMapper.countLowStockWarnings()).thenReturn(1L);

        PageResponse<LowStockWarningVO> response = warningService.queryLowStockWarnings(1, 10);

        assertEquals(1, response.records().size());
        assertEquals(1L, response.total());
    }

    @Test
    void shouldReturnExpiryWarnings() {
        ExpiryWarningQueryRequest request = new ExpiryWarningQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        request.setExpiryDays(30);
        ExpiryWarningVO warningVO = new ExpiryWarningVO();
        warningVO.setInventoryId(1L);
        when(warningMapper.findExpiryWarnings(request)).thenReturn(List.of(warningVO));
        when(warningMapper.countExpiryWarnings(request)).thenReturn(1L);

        PageResponse<ExpiryWarningVO> response = warningService.queryExpiryWarnings(request);

        assertEquals(1, response.records().size());
        assertEquals(1L, response.total());
    }
}
