package com.example.drugmanagement.dto.inventory;

import com.example.drugmanagement.common.dto.PageQuery;

public class InventoryQueryRequest extends PageQuery {

    private Long drugId;
    private String keyword;

    public Long getDrugId() {
        return drugId;
    }

    public void setDrugId(Long drugId) {
        this.drugId = drugId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
}
