package com.example.drugmanagement.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateInventoryCheckRequest {

    @NotNull(message = "must not be null")
    private Long inventoryId;

    @NotNull(message = "must not be null")
    @Min(value = 0, message = "must be greater than or equal to 0")
    private Integer actualQuantity;

    @NotBlank(message = "must not be blank")
    private String bizNo;

    @NotBlank(message = "must not be blank")
    private String operatorName;

    private String remark;

    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    public Integer getActualQuantity() {
        return actualQuantity;
    }

    public void setActualQuantity(Integer actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    public String getBizNo() {
        return bizNo;
    }

    public void setBizNo(String bizNo) {
        this.bizNo = bizNo;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
