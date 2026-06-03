package com.example.drugmanagement.service;

import com.example.drugmanagement.common.enums.InventoryRecordType;
import com.example.drugmanagement.common.exception.BusinessException;
import com.example.drugmanagement.common.response.PageResponse;
import com.example.drugmanagement.dto.inventory.CreateInventoryInboundRequest;
import com.example.drugmanagement.dto.inventory.InventoryQueryRequest;
import com.example.drugmanagement.entity.Drug;
import com.example.drugmanagement.entity.Inventory;
import com.example.drugmanagement.entity.InventoryRecord;
import com.example.drugmanagement.mapper.DrugMapper;
import com.example.drugmanagement.mapper.InventoryMapper;
import com.example.drugmanagement.mapper.InventoryRecordMapper;
import com.example.drugmanagement.service.impl.InventoryServiceImpl;
import com.example.drugmanagement.vo.inventory.InventoryVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private DrugMapper drugMapper;

    @Mock
    private InventoryMapper inventoryMapper;

    @Mock
    private InventoryRecordMapper inventoryRecordMapper;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    void shouldCreateInventoryWhenBatchDoesNotExist() {
        CreateInventoryInboundRequest request = buildInboundRequest();
        Drug drug = new Drug();
        drug.setId(1L);
        when(drugMapper.findEntityById(1L)).thenReturn(drug);
        when(inventoryMapper.findByDrugBatchAndExpiry(1L, "BATCH-001", LocalDate.of(2027, 1, 1))).thenReturn(null);
        doAnswer(invocation -> {
            Inventory inventory = invocation.getArgument(0);
            inventory.setId(10L);
            return 1;
        }).when(inventoryMapper).insert(any(Inventory.class));

        Long inventoryId = inventoryService.inbound(request);

        assertEquals(10L, inventoryId);
        ArgumentCaptor<InventoryRecord> recordCaptor = ArgumentCaptor.forClass(InventoryRecord.class);
        verify(inventoryRecordMapper).insert(recordCaptor.capture());
        assertEquals(InventoryRecordType.INBOUND.name(), recordCaptor.getValue().getRecordType());
        assertEquals(0, recordCaptor.getValue().getBeforeQuantity());
        assertEquals(50, recordCaptor.getValue().getAfterQuantity());
    }

    @Test
    void shouldIncreaseInventoryWhenBatchAlreadyExists() {
        CreateInventoryInboundRequest request = buildInboundRequest();
        Drug drug = new Drug();
        drug.setId(1L);
        Inventory existing = new Inventory();
        existing.setId(11L);
        existing.setQuantity(30);
        when(drugMapper.findEntityById(1L)).thenReturn(drug);
        when(inventoryMapper.findByDrugBatchAndExpiry(1L, "BATCH-001", LocalDate.of(2027, 1, 1))).thenReturn(existing);

        Long inventoryId = inventoryService.inbound(request);

        assertEquals(11L, inventoryId);
        verify(inventoryMapper).increaseQuantity(11L, 50, "A-01", "药师张三");
        ArgumentCaptor<InventoryRecord> recordCaptor = ArgumentCaptor.forClass(InventoryRecord.class);
        verify(inventoryRecordMapper).insert(recordCaptor.capture());
        assertEquals(30, recordCaptor.getValue().getBeforeQuantity());
        assertEquals(80, recordCaptor.getValue().getAfterQuantity());
    }

    @Test
    void shouldRejectInboundWhenDrugDoesNotExist() {
        CreateInventoryInboundRequest request = buildInboundRequest();
        when(drugMapper.findEntityById(1L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> inventoryService.inbound(request));
    }

    @Test
    void shouldReturnInventoryPage() {
        InventoryQueryRequest request = new InventoryQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        InventoryVO inventoryVO = new InventoryVO();
        inventoryVO.setId(1L);
        when(inventoryMapper.findPage(request)).thenReturn(List.of(inventoryVO));
        when(inventoryMapper.count(request)).thenReturn(1L);

        PageResponse<InventoryVO> response = inventoryService.queryInventories(request);

        assertEquals(1, response.records().size());
        assertEquals(1L, response.total());
    }

    @Test
    void shouldReturnInventoryDetail() {
        InventoryVO inventoryVO = new InventoryVO();
        inventoryVO.setId(1L);
        when(inventoryMapper.findVoById(1L)).thenReturn(inventoryVO);

        InventoryVO result = inventoryService.getInventoryById(1L);

        assertEquals(1L, result.getId());
    }

    private CreateInventoryInboundRequest buildInboundRequest() {
        CreateInventoryInboundRequest request = new CreateInventoryInboundRequest();
        request.setDrugId(1L);
        request.setBatchNo("BATCH-001");
        request.setExpiryDate(LocalDate.of(2027, 1, 1));
        request.setQuantity(50);
        request.setLocationCode("A-01");
        request.setBizNo("IN-20260603-001");
        request.setOperatorName("药师张三");
        request.setRemark("首批入库");
        return request;
    }
}
