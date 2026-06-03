package com.example.drugmanagement.service;

import com.example.drugmanagement.common.exception.BusinessException;
import com.example.drugmanagement.common.response.PageResponse;
import com.example.drugmanagement.dto.drug.CreateDrugRequest;
import com.example.drugmanagement.dto.drug.DrugQueryRequest;
import com.example.drugmanagement.dto.drug.UpdateDrugRequest;
import com.example.drugmanagement.entity.Drug;
import com.example.drugmanagement.mapper.DrugMapper;
import com.example.drugmanagement.service.impl.DrugServiceImpl;
import com.example.drugmanagement.vo.drug.DrugVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DrugServiceTest {

    @Mock
    private DrugMapper drugMapper;

    @InjectMocks
    private DrugServiceImpl drugService;

    @Test
    void shouldCreateDrugWhenDrugCodeIsUnique() {
        CreateDrugRequest request = buildCreateRequest();
        when(drugMapper.findByDrugCode("DRUG-100")).thenReturn(null);
        doAnswer(invocation -> {
            Drug drug = invocation.getArgument(0);
            drug.setId(1L);
            return 1;
        }).when(drugMapper).insert(any(Drug.class));

        Long createdId = drugService.createDrug(request);

        assertEquals(1L, createdId);
        ArgumentCaptor<Drug> captor = ArgumentCaptor.forClass(Drug.class);
        verify(drugMapper).insert(captor.capture());
        assertEquals("DRUG-100", captor.getValue().getDrugCode());
        assertEquals("system", captor.getValue().getCreatedBy());
    }

    @Test
    void shouldRejectCreateDrugWhenDrugCodeAlreadyExists() {
        CreateDrugRequest request = buildCreateRequest();
        Drug existing = new Drug();
        existing.setId(10L);
        when(drugMapper.findByDrugCode("DRUG-100")).thenReturn(existing);

        assertThrows(BusinessException.class, () -> drugService.createDrug(request));
    }

    @Test
    void shouldReturnDrugWhenIdExists() {
        DrugVO drugVO = new DrugVO();
        drugVO.setId(1L);
        drugVO.setDrugCode("DRUG-100");
        when(drugMapper.findById(1L)).thenReturn(drugVO);

        DrugVO result = drugService.getDrugById(1L);

        assertEquals(1L, result.getId());
        assertEquals("DRUG-100", result.getDrugCode());
    }

    @Test
    void shouldUpdateDrugWhenIdExists() {
        Drug existing = new Drug();
        existing.setId(1L);
        existing.setDrugCode("DRUG-100");
        when(drugMapper.findEntityById(1L)).thenReturn(existing);
        when(drugMapper.updateById(any(Drug.class))).thenReturn(1);

        drugService.updateDrug(1L, buildUpdateRequest());

        ArgumentCaptor<Drug> captor = ArgumentCaptor.forClass(Drug.class);
        verify(drugMapper).updateById(captor.capture());
        assertEquals(1L, captor.getValue().getId());
        assertEquals("感冒灵颗粒", captor.getValue().getDrugName());
        assertEquals("DRUG-100", captor.getValue().getDrugCode());
    }

    @Test
    void shouldDeleteDrugWhenIdExists() {
        Drug existing = new Drug();
        existing.setId(1L);
        when(drugMapper.findEntityById(1L)).thenReturn(existing);
        when(drugMapper.logicalDeleteById(1L, "system")).thenReturn(1);

        drugService.deleteDrug(1L);

        verify(drugMapper).logicalDeleteById(1L, "system");
    }

    @Test
    void shouldReturnPagedDrugs() {
        DrugQueryRequest request = new DrugQueryRequest();
        request.setPageNum(2);
        request.setPageSize(5);
        DrugVO drugVO = new DrugVO();
        drugVO.setId(1L);
        when(drugMapper.findPage(request)).thenReturn(List.of(drugVO));
        when(drugMapper.count(request)).thenReturn(11L);

        PageResponse<DrugVO> response = drugService.queryDrugs(request);

        assertEquals(1, response.records().size());
        assertEquals(11L, response.total());
        assertEquals(2, response.pageNum());
        assertEquals(5, response.pageSize());
    }

    private CreateDrugRequest buildCreateRequest() {
        CreateDrugRequest request = new CreateDrugRequest();
        request.setDrugCode("DRUG-100");
        request.setDrugName("感冒灵颗粒");
        request.setGenericName("感冒灵");
        request.setCategory("中成药");
        request.setSpecification("10g*9袋");
        request.setUnit("盒");
        request.setManufacturer("示例药厂");
        request.setApprovalNumber("国药准字Z1000001");
        request.setPurchasePrice(new BigDecimal("10.00"));
        request.setSalePrice(new BigDecimal("15.00"));
        request.setLowStockThreshold(5);
        request.setEnabled(1);
        return request;
    }

    private UpdateDrugRequest buildUpdateRequest() {
        UpdateDrugRequest request = new UpdateDrugRequest();
        request.setDrugName("感冒灵颗粒");
        request.setGenericName("感冒灵");
        request.setCategory("中成药");
        request.setSpecification("10g*9袋");
        request.setUnit("盒");
        request.setManufacturer("示例药厂");
        request.setApprovalNumber("国药准字Z1000001");
        request.setPurchasePrice(new BigDecimal("10.00"));
        request.setSalePrice(new BigDecimal("15.00"));
        request.setLowStockThreshold(5);
        request.setEnabled(1);
        return request;
    }
}
