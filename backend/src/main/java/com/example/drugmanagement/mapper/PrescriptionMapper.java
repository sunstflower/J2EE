package com.example.drugmanagement.mapper;

import com.example.drugmanagement.dto.prescription.PrescriptionQueryRequest;
import com.example.drugmanagement.entity.Prescription;
import com.example.drugmanagement.vo.prescription.PrescriptionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PrescriptionMapper {

    int insert(Prescription prescription);

    int updateStatus(@Param("id") Long id,
                     @Param("status") String status,
                     @Param("doctorApprovalStatus") String doctorApprovalStatus,
                     @Param("doctorApprovedAt") LocalDateTime doctorApprovedAt,
                     @Param("auditBy") String auditBy,
                     @Param("auditTime") LocalDateTime auditTime,
                     @Param("dispenseBy") String dispenseBy,
                     @Param("dispenseTime") LocalDateTime dispenseTime,
                     @Param("rejectReason") String rejectReason,
                     @Param("updatedBy") String updatedBy);

    int updateStatusByCurrentStatus(@Param("id") Long id,
                                    @Param("currentStatus") String currentStatus,
                                    @Param("status") String status,
                                    @Param("doctorApprovalStatus") String doctorApprovalStatus,
                                    @Param("doctorApprovedAt") LocalDateTime doctorApprovedAt,
                                    @Param("auditBy") String auditBy,
                                    @Param("auditTime") LocalDateTime auditTime,
                                    @Param("dispenseBy") String dispenseBy,
                                    @Param("dispenseTime") LocalDateTime dispenseTime,
                                    @Param("rejectReason") String rejectReason,
                                    @Param("updatedBy") String updatedBy);

    Prescription findEntityById(@Param("id") Long id);

    PrescriptionVO findById(@Param("id") Long id);

    List<PrescriptionVO> findPage(PrescriptionQueryRequest request);

    long count(PrescriptionQueryRequest request);
}
