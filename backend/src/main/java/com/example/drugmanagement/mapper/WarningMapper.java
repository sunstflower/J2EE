package com.example.drugmanagement.mapper;

import com.example.drugmanagement.dto.warning.ExpiryWarningQueryRequest;
import com.example.drugmanagement.vo.warning.ExpiryWarningVO;
import com.example.drugmanagement.vo.warning.LowStockWarningVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WarningMapper {

    List<LowStockWarningVO> findLowStockWarnings(@Param("pageSize") int pageSize, @Param("offset") int offset);

    long countLowStockWarnings();

    List<ExpiryWarningVO> findExpiryWarnings(ExpiryWarningQueryRequest request);

    long countExpiryWarnings(ExpiryWarningQueryRequest request);
}
