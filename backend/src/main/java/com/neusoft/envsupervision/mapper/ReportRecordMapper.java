package com.neusoft.envsupervision.mapper;

import com.neusoft.envsupervision.domain.ReportRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReportRecordMapper {
    @Select("SELECT * FROM report_records WHERE report_id = #{reportId} ORDER BY created_at, id")
    List<ReportRecord> findByReportId(Long reportId);

    @Insert("""
            INSERT INTO report_records (report_id, action, operator, remark, created_at)
            VALUES (#{reportId}, #{action}, #{operator}, #{remark}, #{createdAt})
            """)
    int insert(
            @Param("reportId") Long reportId,
            @Param("action") String action,
            @Param("operator") String operator,
            @Param("remark") String remark,
            @Param("createdAt") String createdAt
    );
}
