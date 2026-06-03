package com.example.drugmanagement.dto.warning;

import com.example.drugmanagement.common.dto.PageQuery;
import jakarta.validation.constraints.Min;

public class ExpiryWarningQueryRequest extends PageQuery {

    @Min(value = 0, message = "must be greater than or equal to 0")
    private Integer expiryDays = 30;

    public Integer getExpiryDays() {
        return expiryDays;
    }

    public void setExpiryDays(Integer expiryDays) {
        this.expiryDays = expiryDays;
    }
}
