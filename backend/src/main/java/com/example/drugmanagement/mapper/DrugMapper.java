package com.example.drugmanagement.mapper;

import com.example.drugmanagement.dto.drug.DrugQueryRequest;
import com.example.drugmanagement.entity.Drug;
import com.example.drugmanagement.vo.drug.DrugVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DrugMapper {

    int insert(Drug drug);

    int updateById(Drug drug);

    Drug findEntityById(@Param("id") Long id);

    DrugVO findById(@Param("id") Long id);

    Drug findByDrugCode(@Param("drugCode") String drugCode);

    List<DrugVO> findPage(DrugQueryRequest request);

    long count(DrugQueryRequest request);

    int logicalDeleteById(@Param("id") Long id, @Param("updatedBy") String updatedBy);
}
