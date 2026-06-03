package com.example.drugmanagement.controller;

import com.example.drugmanagement.common.response.ApiResponse;
import com.example.drugmanagement.common.response.PageResponse;
import com.example.drugmanagement.dto.inventory.CreateInventoryCheckRequest;
import com.example.drugmanagement.dto.inventory.CreateInventoryInboundRequest;
import com.example.drugmanagement.dto.inventory.CreateInventoryOutboundRequest;
import com.example.drugmanagement.dto.inventory.InventoryQueryRequest;
import com.example.drugmanagement.dto.inventory.InventoryRecordQueryRequest;
import com.example.drugmanagement.service.InventoryService;
import com.example.drugmanagement.vo.inventory.InventoryRecordVO;
import com.example.drugmanagement.vo.inventory.InventoryVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventories")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public ApiResponse<PageResponse<InventoryVO>> queryInventories(@Valid InventoryQueryRequest request) {
        return ApiResponse.success(inventoryService.queryInventories(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<InventoryVO> getInventory(@PathVariable Long id) {
        return ApiResponse.success(inventoryService.getInventoryById(id));
    }

    @PostMapping("/inbound")
    public ApiResponse<Long> inbound(@Valid @RequestBody CreateInventoryInboundRequest request) {
        return ApiResponse.success(inventoryService.inbound(request));
    }

    @PostMapping("/outbound")
    public ApiResponse<Void> outbound(@Valid @RequestBody CreateInventoryOutboundRequest request) {
        inventoryService.outbound(request);
        return ApiResponse.success();
    }

    @PostMapping("/check")
    public ApiResponse<Void> check(@Valid @RequestBody CreateInventoryCheckRequest request) {
        inventoryService.check(request);
        return ApiResponse.success();
    }

    @GetMapping("/records")
    public ApiResponse<PageResponse<InventoryRecordVO>> queryInventoryRecords(@Valid InventoryRecordQueryRequest request) {
        return ApiResponse.success(inventoryService.queryInventoryRecords(request));
    }
}
