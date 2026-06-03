package com.example.drugmanagement.dto.drug;

import com.example.drugmanagement.common.dto.PageQuery;

public class DrugQueryRequest extends PageQuery {

    private String keyword;
    private String category;
    private Integer enabled;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }
}
