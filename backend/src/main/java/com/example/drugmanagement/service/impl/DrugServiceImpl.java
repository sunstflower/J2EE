package com.example.drugmanagement.service.impl;

import com.example.drugmanagement.common.exception.BusinessException;
import com.example.drugmanagement.common.response.PageResponse;
import com.example.drugmanagement.common.response.ResponseCode;
import com.example.drugmanagement.dto.drug.CreateDrugRequest;
import com.example.drugmanagement.dto.drug.DrugQueryRequest;
import com.example.drugmanagement.dto.drug.UpdateDrugRequest;
import com.example.drugmanagement.entity.Drug;
import com.example.drugmanagement.mapper.DrugMapper;
import com.example.drugmanagement.service.DrugService;
import com.example.drugmanagement.vo.drug.DrugVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DrugServiceImpl implements DrugService {

    private static final String SYSTEM_OPERATOR = "system";

    private final DrugMapper drugMapper;

    public DrugServiceImpl(DrugMapper drugMapper) {
        this.drugMapper = drugMapper;
    }

    @Override
    @Transactional
    public Long createDrug(CreateDrugRequest request) {
        validateDrugCodeUnique(request.getDrugCode(), null);

        Drug drug = new Drug();
        drug.setDrugCode(request.getDrugCode());
        applyMutableFields(drug, request);
        drug.setCreatedBy(SYSTEM_OPERATOR);
        drug.setUpdatedBy(SYSTEM_OPERATOR);
        drug.setDeleted(0);

        drugMapper.insert(drug);
        return drug.getId();
    }

    @Override
    public DrugVO getDrugById(Long id) {
        DrugVO drugVO = drugMapper.findById(id);
        if (drugVO == null) {
            throw BusinessException.of(ResponseCode.RESOURCE_NOT_FOUND);
        }
        return drugVO;
    }

    @Override
    public PageResponse<DrugVO> queryDrugs(DrugQueryRequest request) {
        List<DrugVO> records = drugMapper.findPage(request);
        long total = drugMapper.count(request);
        return PageResponse.of(records, total, request.getPageNum(), request.getPageSize());
    }

    @Override
    @Transactional
    public void updateDrug(Long id, UpdateDrugRequest request) {
        Drug existing = getExistingDrug(id);

        Drug toUpdate = new Drug();
        toUpdate.setId(existing.getId());
        toUpdate.setDrugCode(existing.getDrugCode());
        applyMutableFields(toUpdate, request);
        toUpdate.setUpdatedBy(SYSTEM_OPERATOR);

        drugMapper.updateById(toUpdate);
    }

    @Override
    @Transactional
    public void deleteDrug(Long id) {
        getExistingDrug(id);
        int affectedRows = drugMapper.logicalDeleteById(id, SYSTEM_OPERATOR);
        if (affectedRows == 0) {
            throw BusinessException.of(ResponseCode.RESOURCE_NOT_FOUND);
        }
    }

    private Drug getExistingDrug(Long id) {
        Drug existing = drugMapper.findEntityById(id);
        if (existing == null) {
            throw BusinessException.of(ResponseCode.RESOURCE_NOT_FOUND);
        }
        return existing;
    }

    private void validateDrugCodeUnique(String drugCode, Long currentId) {
        Drug existing = drugMapper.findByDrugCode(drugCode);
        if (existing != null && !existing.getId().equals(currentId)) {
            throw BusinessException.of(ResponseCode.BUSINESS_RULE_VIOLATION);
        }
    }

    private void applyMutableFields(Drug target, CreateDrugRequest request) {
        target.setDrugName(request.getDrugName());
        target.setGenericName(request.getGenericName());
        target.setCategory(request.getCategory());
        target.setSpecification(request.getSpecification());
        target.setUnit(request.getUnit());
        target.setManufacturer(request.getManufacturer());
        target.setApprovalNumber(request.getApprovalNumber());
        target.setPurchasePrice(request.getPurchasePrice());
        target.setSalePrice(request.getSalePrice());
        target.setLowStockThreshold(request.getLowStockThreshold());
        target.setEnabled(request.getEnabled());
    }

    private void applyMutableFields(Drug target, UpdateDrugRequest request) {
        target.setDrugName(request.getDrugName());
        target.setGenericName(request.getGenericName());
        target.setCategory(request.getCategory());
        target.setSpecification(request.getSpecification());
        target.setUnit(request.getUnit());
        target.setManufacturer(request.getManufacturer());
        target.setApprovalNumber(request.getApprovalNumber());
        target.setPurchasePrice(request.getPurchasePrice());
        target.setSalePrice(request.getSalePrice());
        target.setLowStockThreshold(request.getLowStockThreshold());
        target.setEnabled(request.getEnabled());
    }
}
