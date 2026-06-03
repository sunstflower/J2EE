package com.example.drugmanagement.mapper;

import com.example.drugmanagement.entity.PrescriptionItem;
import com.example.drugmanagement.vo.prescription.PrescriptionItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrescriptionItemMapper {

    int insertBatch(@Param("items") List<PrescriptionItem> items);

    List<PrescriptionItemVO> findByPrescriptionId(@Param("prescriptionId") Long prescriptionId);
}
