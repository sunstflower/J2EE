package com.example.drugmanagement.dto.inventory;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateInventoryOutboundRequest {

    @NotNull(message = "must not be null")
    private Long inventoryId;

    @NotNull(message = "must not be null")
    @Min(value = 1, message = "must be greater than or equal to 1")
    private Integer quantity;

    @NotBlank(message = "must not be blank")
    private String bizNo;

    private String remark;

    public Long getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(Long inventoryId) {
        this.inventoryId = inventoryId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getBizNo() {
        return bizNo;
    }

    public void setBizNo(String bizNo) {
        this.bizNo = bizNo;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
