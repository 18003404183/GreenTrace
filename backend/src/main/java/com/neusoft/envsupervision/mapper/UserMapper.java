package com.neusoft.envsupervision.mapper;

import com.neusoft.envsupervision.domain.UserAccount;
import com.neusoft.envsupervision.dto.CreateUserRequest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM user_accounts ORDER BY id")
    List<UserAccount> findAll();

    @Select("SELECT * FROM user_accounts WHERE username = #{username}")
    UserAccount findByUsername(String username);

    @Insert("""
            INSERT INTO user_accounts (username, password, real_name, role, area_name, status)
            VALUES (#{request.username}, #{request.password}, #{request.realName}, #{request.role}, #{request.areaName}, '启用')
            """)
    int insert(@Param("request") CreateUserRequest request);
}
