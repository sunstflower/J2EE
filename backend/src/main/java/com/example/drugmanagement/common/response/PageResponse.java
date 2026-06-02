package com.example.drugmanagement.common.response;

import java.util.List;

public record PageResponse<T>(
        List<T> records,
        long total,
        int pageNum,
        int pageSize
) {

    public static <T> PageResponse<T> of(List<T> records, long total, int pageNum, int pageSize) {
        return new PageResponse<>(records, total, pageNum, pageSize);
    }
}
