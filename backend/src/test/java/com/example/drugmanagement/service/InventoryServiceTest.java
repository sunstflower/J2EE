package com.example.drugmanagement.service;

import com.example.drugmanagement.common.enums.InventoryRecordType;
import com.example.drugmanagement.common.exception.BusinessException;
import com.example.drugmanagement.common.response.PageResponse;
import com.example.drugmanagement.dto.inventory.CreateInventoryCheckRequest;
import com.example.drugmanagement.dto.inventory.CreateInventoryInboundRequest;
import com.example.drugmanagement.dto.inventory.InventoryQueryRequest;
import com.example.drugmanagement.dto.inventory.CreateInventoryOutboundRequest;
import com.example.drugmanagement.dto.inventory.InventoryRecordQueryRequest;
import com.example.drugmanagement.entity.Drug;
import com.example.drugmanagement.entity.Inventory;
import com.example.drugmanagement.entity.InventoryRecord;
import com.example.drugmanagement.mapper.DrugMapper;
import com.example.drugmanagement.mapper.InventoryMapper;
import com.example.drugmanagement.mapper.InventoryRecordMapper;
import com.example.drugmanagement.service.impl.InventoryServiceImpl;
import com.example.drugmanagement.vo.inventory.InventoryRecordVO;
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

    @Test
    void shouldOutboundByEarliestExpiryFirst() {
        CreateInventoryOutboundRequest request = new CreateInventoryOutboundRequest();
        request.setDrugId(1L);
        request.setQuantity(60);
        request.setBizNo("OUT-20260603-001");
        request.setOperatorName("药师李四");
        request.setRemark("窗口发药");

        Drug drug = new Drug();
        drug.setId(1L);
        Inventory inventory1 = new Inventory();
        inventory1.setId(100L);
        inventory1.setDrugId(1L);
        inventory1.setQuantity(30);
        inventory1.setLockedQuantity(0);
        Inventory inventory2 = new Inventory();
        inventory2.setId(101L);
        inventory2.setDrugId(1L);
        inventory2.setQuantity(50);
        inventory2.setLockedQuantity(0);

        when(drugMapper.findEntityById(1L)).thenReturn(drug);
        when(inventoryMapper.findAvailableByDrugIdOrderByExpiry(1L)).thenReturn(List.of(inventory1, inventory2));

        inventoryService.outbound(request);

        verify(inventoryMapper).decreaseQuantity(100L, 30, "药师李四");
        verify(inventoryMapper).decreaseQuantity(101L, 30, "药师李四");
    }

    @Test
    void shouldRejectOutboundWhenInventoryInsufficient() {
        CreateInventoryOutboundRequest request = new CreateInventoryOutboundRequest();
        request.setDrugId(1L);
        request.setQuantity(100);
        request.setBizNo("OUT-20260603-001");
        request.setOperatorName("药师李四");

        Drug drug = new Drug();
        drug.setId(1L);
        Inventory inventory = new Inventory();
        inventory.setId(100L);
        inventory.setDrugId(1L);
        inventory.setQuantity(20);
        inventory.setLockedQuantity(0);

        when(drugMapper.findEntityById(1L)).thenReturn(drug);
        when(inventoryMapper.findAvailableByDrugIdOrderByExpiry(1L)).thenReturn(List.of(inventory));

        assertThrows(BusinessException.class, () -> inventoryService.outbound(request));
    }

    @Test
    void shouldCheckInventoryAndWriteRecord() {
        CreateInventoryCheckRequest request = new CreateInventoryCheckRequest();
        request.setInventoryId(10L);
        request.setActualQuantity(18);
        request.setBizNo("CHK-20260603-001");
        request.setOperatorName("药师李四");
        request.setRemark("月度盘点");

        Inventory inventory = new Inventory();
        inventory.setId(10L);
        inventory.setDrugId(1L);
        inventory.setQuantity(20);
        when(inventoryMapper.findById(10L)).thenReturn(inventory);

        inventoryService.check(request);

        verify(inventoryMapper).updateQuantityByCheck(10L, 18, "药师李四");
        ArgumentCaptor<InventoryRecord> recordCaptor = ArgumentCaptor.forClass(InventoryRecord.class);
        verify(inventoryRecordMapper).insert(recordCaptor.capture());
        assertEquals(InventoryRecordType.CHECK.name(), recordCaptor.getValue().getRecordType());
        assertEquals(-2, recordCaptor.getValue().getQuantityChange());
    }

    @Test
    void shouldReturnInventoryRecordPage() {
        InventoryRecordQueryRequest request = new InventoryRecordQueryRequest();
        request.setPageNum(1);
        request.setPageSize(10);
        InventoryRecordVO recordVO = new InventoryRecordVO();
        recordVO.setId(1L);
        when(inventoryRecordMapper.findPage(request)).thenReturn(List.of(recordVO));
        when(inventoryRecordMapper.count(request)).thenReturn(1L);

        PageResponse<InventoryRecordVO> response = inventoryService.queryInventoryRecords(request);

        assertEquals(1, response.records().size());
        assertEquals(1L, response.total());
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
