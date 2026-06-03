package com.example.drugmanagement.service.impl;

import com.example.drugmanagement.common.enums.InventoryRecordType;
import com.example.drugmanagement.common.exception.BusinessException;
import com.example.drugmanagement.common.response.PageResponse;
import com.example.drugmanagement.common.response.ResponseCode;
import com.example.drugmanagement.dto.inventory.CreateInventoryInboundRequest;
import com.example.drugmanagement.dto.inventory.InventoryQueryRequest;
import com.example.drugmanagement.entity.Drug;
import com.example.drugmanagement.entity.Inventory;
import com.example.drugmanagement.entity.InventoryRecord;
import com.example.drugmanagement.mapper.DrugMapper;
import com.example.drugmanagement.mapper.InventoryMapper;
import com.example.drugmanagement.mapper.InventoryRecordMapper;
import com.example.drugmanagement.service.InventoryService;
import com.example.drugmanagement.vo.inventory.InventoryVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final DrugMapper drugMapper;
    private final InventoryMapper inventoryMapper;
    private final InventoryRecordMapper inventoryRecordMapper;

    public InventoryServiceImpl(DrugMapper drugMapper,
                                InventoryMapper inventoryMapper,
                                InventoryRecordMapper inventoryRecordMapper) {
        this.drugMapper = drugMapper;
        this.inventoryMapper = inventoryMapper;
        this.inventoryRecordMapper = inventoryRecordMapper;
    }

    @Override
    @Transactional
    public Long inbound(CreateInventoryInboundRequest request) {
        Drug drug = drugMapper.findEntityById(request.getDrugId());
        if (drug == null) {
            throw BusinessException.of(ResponseCode.RESOURCE_NOT_FOUND);
        }

        Inventory existing = inventoryMapper.findByDrugBatchAndExpiry(
                request.getDrugId(),
                request.getBatchNo(),
                request.getExpiryDate()
        );

        Long inventoryId;
        int beforeQuantity;
        int afterQuantity;

        if (existing == null) {
            Inventory inventory = new Inventory();
            inventory.setDrugId(request.getDrugId());
            inventory.setBatchNo(request.getBatchNo());
            inventory.setExpiryDate(request.getExpiryDate());
            inventory.setQuantity(request.getQuantity());
            inventory.setLockedQuantity(0);
            inventory.setLocationCode(request.getLocationCode());
            inventory.setCreatedBy(request.getOperatorName());
            inventory.setUpdatedBy(request.getOperatorName());
            inventory.setDeleted(0);
            inventoryMapper.insert(inventory);

            inventoryId = inventory.getId();
            beforeQuantity = 0;
            afterQuantity = request.getQuantity();
        } else {
            beforeQuantity = existing.getQuantity();
            afterQuantity = beforeQuantity + request.getQuantity();
            inventoryMapper.increaseQuantity(
                    existing.getId(),
                    request.getQuantity(),
                    request.getLocationCode(),
                    request.getOperatorName()
            );
            inventoryId = existing.getId();
        }

        InventoryRecord inventoryRecord = new InventoryRecord();
        inventoryRecord.setDrugId(request.getDrugId());
        inventoryRecord.setInventoryId(inventoryId);
        inventoryRecord.setRecordType(InventoryRecordType.INBOUND.name());
        inventoryRecord.setQuantityChange(request.getQuantity());
        inventoryRecord.setBeforeQuantity(beforeQuantity);
        inventoryRecord.setAfterQuantity(afterQuantity);
        inventoryRecord.setBizNo(request.getBizNo());
        inventoryRecord.setOperatorName(request.getOperatorName());
        inventoryRecord.setOperatedAt(LocalDateTime.now());
        inventoryRecord.setRemark(request.getRemark());
        inventoryRecord.setCreatedBy(request.getOperatorName());
        inventoryRecord.setUpdatedBy(request.getOperatorName());
        inventoryRecord.setDeleted(0);
        inventoryRecordMapper.insert(inventoryRecord);

        return inventoryId;
    }

    @Override
    public PageResponse<InventoryVO> queryInventories(InventoryQueryRequest request) {
        List<InventoryVO> records = inventoryMapper.findPage(request);
        long total = inventoryMapper.count(request);
        return PageResponse.of(records, total, request.getPageNum(), request.getPageSize());
    }

    @Override
    public InventoryVO getInventoryById(Long id) {
        InventoryVO inventoryVO = inventoryMapper.findVoById(id);
        if (inventoryVO == null) {
            throw BusinessException.of(ResponseCode.RESOURCE_NOT_FOUND);
        }
        return inventoryVO;
    }
}
