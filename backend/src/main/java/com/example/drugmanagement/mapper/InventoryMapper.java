package com.example.drugmanagement.mapper;

import com.example.drugmanagement.dto.inventory.InventoryQueryRequest;
import com.example.drugmanagement.entity.Inventory;
import com.example.drugmanagement.vo.inventory.InventoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface InventoryMapper {

    Inventory findById(@Param("id") Long id);

    Inventory findByDrugBatchAndExpiry(@Param("drugId") Long drugId,
                                       @Param("batchNo") String batchNo,
                                       @Param("expiryDate") LocalDate expiryDate);

    int insert(Inventory inventory);

    int increaseQuantity(@Param("id") Long id,
                         @Param("quantity") Integer quantity,
                         @Param("locationCode") String locationCode,
                         @Param("updatedBy") String updatedBy);

    int decreaseQuantity(@Param("id") Long id,
                         @Param("quantity") Integer quantity,
                         @Param("updatedBy") String updatedBy);

    int updateQuantityByCheck(@Param("id") Long id,
                              @Param("actualQuantity") Integer actualQuantity,
                              @Param("updatedBy") String updatedBy);

    List<Inventory> findAvailableByDrugIdOrderByExpiry(@Param("drugId") Long drugId);

    List<InventoryVO> findPage(InventoryQueryRequest request);

    long count(InventoryQueryRequest request);

    InventoryVO findVoById(@Param("id") Long id);
}
