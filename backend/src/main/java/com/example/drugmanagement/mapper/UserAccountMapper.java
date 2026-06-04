package com.example.drugmanagement.mapper;

import com.example.drugmanagement.entity.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserAccountMapper {

    UserAccount findByUserId(@Param("userId") Long userId);

    int insert(UserAccount userAccount);
}
