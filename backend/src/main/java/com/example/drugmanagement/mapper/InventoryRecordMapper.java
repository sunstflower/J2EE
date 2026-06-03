package com.example.drugmanagement.mapper;

import com.example.drugmanagement.dto.inventory.InventoryRecordQueryRequest;
import com.example.drugmanagement.entity.InventoryRecord;
import com.example.drugmanagement.vo.inventory.InventoryRecordVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InventoryRecordMapper {

    int insert(InventoryRecord inventoryRecord);

    List<InventoryRecordVO> findPage(InventoryRecordQueryRequest request);

    long count(InventoryRecordQueryRequest request);
}
