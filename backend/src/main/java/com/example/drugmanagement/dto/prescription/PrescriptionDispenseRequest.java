package com.example.drugmanagement.dto.prescription;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PrescriptionDispenseRequest {

    @NotBlank(message = "must not be blank")
    private String operatorName;

    @NotNull(message = "must not be null")
    private Long operatorId;

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }
}
