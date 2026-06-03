package com.example.drugmanagement.mapper;

import com.example.drugmanagement.entity.InventoryRecord;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface InventoryRecordMapper {

    int insert(InventoryRecord inventoryRecord);
}
