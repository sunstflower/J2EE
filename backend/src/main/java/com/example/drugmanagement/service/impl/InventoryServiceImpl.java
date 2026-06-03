package com.example.drugmanagement.service.impl;

import com.example.drugmanagement.common.enums.InventoryRecordType;
import com.example.drugmanagement.common.exception.BusinessException;
import com.example.drugmanagement.common.response.PageResponse;
import com.example.drugmanagement.common.response.ResponseCode;
import com.example.drugmanagement.dto.inventory.CreateInventoryCheckRequest;
import com.example.drugmanagement.dto.inventory.CreateInventoryInboundRequest;
import com.example.drugmanagement.dto.inventory.CreateInventoryOutboundRequest;
import com.example.drugmanagement.dto.inventory.InventoryQueryRequest;
import com.example.drugmanagement.dto.inventory.InventoryRecordQueryRequest;
import com.example.drugmanagement.entity.Drug;
import com.example.drugmanagement.entity.Inventory;
import com.example.drugmanagement.entity.InventoryRecord;
import com.example.drugmanagement.mapper.DrugMapper;
import com.example.drugmanagement.mapper.InventoryMapper;
import com.example.drugmanagement.mapper.InventoryRecordMapper;
import com.example.drugmanagement.service.InventoryService;
import com.example.drugmanagement.vo.inventory.InventoryRecordVO;
import com.example.drugmanagement.vo.inventory.InventoryVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    @Transactional
    public void outbound(CreateInventoryOutboundRequest request) {
        Drug drug = drugMapper.findEntityById(request.getDrugId());
        if (drug == null) {
            throw BusinessException.of(ResponseCode.RESOURCE_NOT_FOUND);
        }

        List<Inventory> inventories = inventoryMapper.findAvailableByDrugIdOrderByExpiry(request.getDrugId());
        int remaining = request.getQuantity();
        List<InventoryRecord> records = new ArrayList<>();

        for (Inventory inventory : inventories) {
            if (remaining <= 0) {
                break;
            }

            int available = inventory.getQuantity() - inventory.getLockedQuantity();
            if (available <= 0) {
                continue;
            }

            int deduction = Math.min(available, remaining);
            int beforeQuantity = inventory.getQuantity();
            int afterQuantity = beforeQuantity - deduction;

            inventoryMapper.decreaseQuantity(inventory.getId(), deduction, request.getOperatorName());

            InventoryRecord record = new InventoryRecord();
            record.setDrugId(request.getDrugId());
            record.setInventoryId(inventory.getId());
            record.setRecordType(InventoryRecordType.OUTBOUND.name());
            record.setQuantityChange(-deduction);
            record.setBeforeQuantity(beforeQuantity);
            record.setAfterQuantity(afterQuantity);
            record.setBizNo(request.getBizNo());
            record.setOperatorName(request.getOperatorName());
            record.setOperatedAt(LocalDateTime.now());
            record.setRemark(request.getRemark());
            record.setCreatedBy(request.getOperatorName());
            record.setUpdatedBy(request.getOperatorName());
            record.setDeleted(0);
            records.add(record);

            remaining -= deduction;
        }

        if (remaining > 0) {
            throw BusinessException.of(ResponseCode.BUSINESS_RULE_VIOLATION);
        }

        for (InventoryRecord record : records) {
            inventoryRecordMapper.insert(record);
        }
    }

    @Override
    @Transactional
    public void check(CreateInventoryCheckRequest request) {
        Inventory inventory = inventoryMapper.findById(request.getInventoryId());
        if (inventory == null) {
            throw BusinessException.of(ResponseCode.RESOURCE_NOT_FOUND);
        }

        int beforeQuantity = inventory.getQuantity();
        int afterQuantity = request.getActualQuantity();
        int change = afterQuantity - beforeQuantity;

        inventoryMapper.updateQuantityByCheck(
                request.getInventoryId(),
                request.getActualQuantity(),
                request.getOperatorName()
        );

        InventoryRecord inventoryRecord = new InventoryRecord();
        inventoryRecord.setDrugId(inventory.getDrugId());
        inventoryRecord.setInventoryId(inventory.getId());
        inventoryRecord.setRecordType(InventoryRecordType.CHECK.name());
        inventoryRecord.setQuantityChange(change);
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

    @Override
    public PageResponse<InventoryRecordVO> queryInventoryRecords(InventoryRecordQueryRequest request) {
        List<InventoryRecordVO> records = inventoryRecordMapper.findPage(request);
        long total = inventoryRecordMapper.count(request);
        return PageResponse.of(records, total, request.getPageNum(), request.getPageSize());
    }
}
