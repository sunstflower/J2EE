package com.example.drugmanagement.dto.drug;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CreateDrugRequest {

    @NotBlank(message = "must not be blank")
    private String drugCode;

    @NotBlank(message = "must not be blank")
    private String drugName;

    private String genericName;
    private String category;
    private String specification;

    @NotBlank(message = "must not be blank")
    private String unit;

    private String manufacturer;
    private String approvalNumber;

    @NotNull(message = "must not be null")
    @DecimalMin(value = "0.00", message = "must be greater than or equal to 0")
    private BigDecimal purchasePrice;

    @NotNull(message = "must not be null")
    @DecimalMin(value = "0.00", message = "must be greater than or equal to 0")
    private BigDecimal salePrice;

    @NotNull(message = "must not be null")
    @Min(value = 0, message = "must be greater than or equal to 0")
    private Integer lowStockThreshold;

    @NotNull(message = "must not be null")
    private Integer enabled;

    public String getDrugCode() {
        return drugCode;
    }

    public void setDrugCode(String drugCode) {
        this.drugCode = drugCode;
    }

    public String getDrugName() {
        return drugName;
    }

    public void setDrugName(String drugName) {
        this.drugName = drugName;
    }

    public String getGenericName() {
        return genericName;
    }

    public void setGenericName(String genericName) {
        this.genericName = genericName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSpecification() {
        return specification;
    }

    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getApprovalNumber() {
        return approvalNumber;
    }

    public void setApprovalNumber(String approvalNumber) {
        this.approvalNumber = approvalNumber;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(BigDecimal salePrice) {
        this.salePrice = salePrice;
    }

    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }
}
