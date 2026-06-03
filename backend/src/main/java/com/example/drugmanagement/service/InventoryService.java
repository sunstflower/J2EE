package com.example.drugmanagement.service;

import com.example.drugmanagement.common.response.PageResponse;
import com.example.drugmanagement.dto.inventory.CreateInventoryCheckRequest;
import com.example.drugmanagement.dto.inventory.CreateInventoryInboundRequest;
import com.example.drugmanagement.dto.inventory.CreateInventoryOutboundRequest;
import com.example.drugmanagement.dto.inventory.InventoryQueryRequest;
import com.example.drugmanagement.dto.inventory.InventoryRecordQueryRequest;
import com.example.drugmanagement.vo.inventory.InventoryRecordVO;
import com.example.drugmanagement.vo.inventory.InventoryVO;

public interface InventoryService {

    Long inbound(CreateInventoryInboundRequest request);

    void outbound(CreateInventoryOutboundRequest request);

    void check(CreateInventoryCheckRequest request);

    PageResponse<InventoryVO> queryInventories(InventoryQueryRequest request);

    InventoryVO getInventoryById(Long id);

    PageResponse<InventoryRecordVO> queryInventoryRecords(InventoryRecordQueryRequest request);
}
