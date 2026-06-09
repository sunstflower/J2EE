package com.example.drugmanagement.service.impl;

import com.example.drugmanagement.common.auth.CurrentUser;
import com.example.drugmanagement.common.auth.CurrentUserHolder;
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
import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryMapper inventoryMapper;
    private final InventoryRecordMapper inventoryRecordMapper;
    private final DrugMapper drugMapper;

    public InventoryServiceImpl(InventoryMapper inventoryMapper,
                                InventoryRecordMapper inventoryRecordMapper,
                                DrugMapper drugMapper) {
        this.inventoryMapper = inventoryMapper;
        this.inventoryRecordMapper = inventoryRecordMapper;
        this.drugMapper = drugMapper;
    }

    @Override
    @Transactional
    public Long inbound(CreateInventoryInboundRequest request) {
        ensureDrugExists(request.getDrugId());
        Inventory existing = inventoryMapper.findByDrugBatchAndExpiry(
                request.getDrugId(),
                request.getBatchNo(),
                request.getExpiryDate()
        );
        String operator = currentOperatorName();
        Long inventoryId;
        int beforeQuantity;

        if (existing == null) {
            Inventory inventory = new Inventory();
            inventory.setDrugId(request.getDrugId());
            inventory.setBatchNo(request.getBatchNo());
            inventory.setExpiryDate(request.getExpiryDate());
            inventory.setQuantity(request.getQuantity());
            inventory.setLockedQuantity(0);
            inventory.setLocationCode(request.getLocationCode());
            inventory.setCreatedBy(operator);
            inventory.setUpdatedBy(operator);
            inventory.setDeleted(0);
            inventoryMapper.insert(inventory);
            inventoryId = inventory.getId();
            beforeQuantity = 0;
        } else {
            beforeQuantity = existing.getQuantity();
            inventoryMapper.increaseQuantity(existing.getId(), request.getQuantity(), request.getLocationCode(), operator);
            inventoryId = existing.getId();
        }

        writeRecord(
                request.getDrugId(),
                inventoryId,
                InventoryRecordType.INBOUND.name(),
                request.getQuantity(),
                beforeQuantity,
                beforeQuantity + request.getQuantity(),
                blankToDefault(request.getBizNo(), "INBOUND"),
                operator,
                request.getRemark()
        );
        return inventoryId;
    }

    @Override
    @Transactional
    public void outbound(CreateInventoryOutboundRequest request) {
        Inventory inventory = getExistingInventory(request.getInventoryId());
        if (inventory.getQuantity() < request.getQuantity()) {
            throw BusinessException.of(ResponseCode.BUSINESS_RULE_VIOLATION);
        }

        String operator = currentOperatorName();
        int affectedRows = inventoryMapper.decreaseQuantity(inventory.getId(), request.getQuantity(), operator);
        if (affectedRows == 0) {
            throw BusinessException.of(ResponseCode.BUSINESS_RULE_VIOLATION);
        }

        writeRecord(
                inventory.getDrugId(),
                inventory.getId(),
                InventoryRecordType.OUTBOUND.name(),
                -request.getQuantity(),
                inventory.getQuantity(),
                inventory.getQuantity() - request.getQuantity(),
                request.getBizNo(),
                operator,
                request.getRemark()
        );
    }

    @Override
    @Transactional
    public void check(CreateInventoryCheckRequest request) {
        Inventory inventory = getExistingInventory(request.getInventoryId());
        String operator = currentOperatorName();
        inventoryMapper.updateQuantityByCheck(inventory.getId(), request.getActualQuantity(), operator);

        writeRecord(
                inventory.getDrugId(),
                inventory.getId(),
                InventoryRecordType.CHECK.name(),
                request.getActualQuantity() - inventory.getQuantity(),
                inventory.getQuantity(),
                request.getActualQuantity(),
                blankToDefault(request.getBizNo(), "CHECK"),
                operator,
                request.getRemark()
        );
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

    private void ensureDrugExists(Long drugId) {
        Drug drug = drugMapper.findEntityById(drugId);
        if (drug == null) {
            throw BusinessException.of(ResponseCode.RESOURCE_NOT_FOUND);
        }
    }

    private Inventory getExistingInventory(Long inventoryId) {
        Inventory inventory = inventoryMapper.findById(inventoryId);
        if (inventory == null) {
            throw BusinessException.of(ResponseCode.RESOURCE_NOT_FOUND);
        }
        return inventory;
    }

    private void writeRecord(Long drugId,
                             Long inventoryId,
                             String recordType,
                             Integer quantityChange,
                             Integer beforeQuantity,
                             Integer afterQuantity,
                             String bizNo,
                             String operator,
                             String remark) {
        InventoryRecord inventoryRecord = new InventoryRecord();
        inventoryRecord.setDrugId(drugId);
        inventoryRecord.setInventoryId(inventoryId);
        inventoryRecord.setRecordType(recordType);
        inventoryRecord.setQuantityChange(quantityChange);
        inventoryRecord.setBeforeQuantity(beforeQuantity);
        inventoryRecord.setAfterQuantity(afterQuantity);
        inventoryRecord.setBizNo(bizNo);
        inventoryRecord.setOperatorName(operator);
        inventoryRecord.setOperatedAt(LocalDateTime.now());
        inventoryRecord.setRemark(remark);
        inventoryRecord.setCreatedBy(operator);
        inventoryRecord.setUpdatedBy(operator);
        inventoryRecord.setDeleted(0);
        inventoryRecordMapper.insert(inventoryRecord);
    }

    private String currentOperatorName() {
        CurrentUser currentUser = CurrentUserHolder.get();
        return currentUser == null ? "system" : currentUser.userName();
    }

    private String blankToDefault(String value, String prefix) {
        if (value == null || value.isBlank()) {
            return prefix + "-" + System.currentTimeMillis();
        }
        return value;
    }
}
