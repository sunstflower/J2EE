package com.example.drugmanagement.dto.inventory;

import com.example.drugmanagement.common.dto.PageQuery;

public class InventoryRecordQueryRequest extends PageQuery {

    private Long drugId;
    private String recordType;
    private String bizNo;

    public Long getDrugId() {
        return drugId;
    }

    public void setDrugId(Long drugId) {
        this.drugId = drugId;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getBizNo() {
        return bizNo;
    }

    public void setBizNo(String bizNo) {
        this.bizNo = bizNo;
    }
}
