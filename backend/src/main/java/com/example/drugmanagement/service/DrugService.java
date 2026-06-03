package com.example.drugmanagement.service;

import com.example.drugmanagement.common.response.PageResponse;
import com.example.drugmanagement.dto.drug.CreateDrugRequest;
import com.example.drugmanagement.dto.drug.DrugQueryRequest;
import com.example.drugmanagement.dto.drug.UpdateDrugRequest;
import com.example.drugmanagement.vo.drug.DrugVO;

public interface DrugService {

    Long createDrug(CreateDrugRequest request);

    DrugVO getDrugById(Long id);

    PageResponse<DrugVO> queryDrugs(DrugQueryRequest request);

    void updateDrug(Long id, UpdateDrugRequest request);

    void deleteDrug(Long id);
}
