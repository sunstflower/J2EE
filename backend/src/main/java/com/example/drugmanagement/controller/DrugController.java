package com.example.drugmanagement.controller;

import com.example.drugmanagement.common.response.ApiResponse;
import com.example.drugmanagement.common.response.PageResponse;
import com.example.drugmanagement.dto.drug.CreateDrugRequest;
import com.example.drugmanagement.dto.drug.DrugQueryRequest;
import com.example.drugmanagement.dto.drug.UpdateDrugRequest;
import com.example.drugmanagement.service.DrugService;
import com.example.drugmanagement.vo.drug.DrugVO;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/drugs")
public class DrugController {

    private final DrugService drugService;

    public DrugController(DrugService drugService) {
        this.drugService = drugService;
    }

    @GetMapping
    public ApiResponse<PageResponse<DrugVO>> queryDrugs(@Valid DrugQueryRequest request) {
        return ApiResponse.success(drugService.queryDrugs(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<DrugVO> getDrug(@PathVariable Long id) {
        return ApiResponse.success(drugService.getDrugById(id));
    }

    @PostMapping
    public ApiResponse<Long> createDrug(@Valid @RequestBody CreateDrugRequest request) {
        return ApiResponse.success(drugService.createDrug(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updateDrug(@PathVariable Long id, @Valid @RequestBody UpdateDrugRequest request) {
        drugService.updateDrug(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDrug(@PathVariable Long id) {
        drugService.deleteDrug(id);
        return ApiResponse.success();
    }
}
