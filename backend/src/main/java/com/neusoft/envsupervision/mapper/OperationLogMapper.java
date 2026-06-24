package com.neusoft.envsupervision.mapper;

import com.neusoft.envsupervision.domain.OperationLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OperationLogMapper {
    @Select("SELECT * FROM operation_logs ORDER BY created_at DESC, id DESC")
    List<OperationLog> findAll();

    @Insert("""
            INSERT INTO operation_logs (operator, module, action, target, detail, created_at)
            VALUES (#{operator}, #{module}, #{action}, #{target}, #{detail}, #{createdAt})
            """)
    int insert(
            @Param("operator") String operator,
            @Param("module") String module,
            @Param("action") String action,
            @Param("target") String target,
            @Param("detail") String detail,
            @Param("createdAt") String createdAt
    );
}
