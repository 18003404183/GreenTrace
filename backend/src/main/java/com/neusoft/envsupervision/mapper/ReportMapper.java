package com.neusoft.envsupervision.mapper;

import com.neusoft.envsupervision.domain.Report;
import com.neusoft.envsupervision.dto.CreateReportRequest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ReportMapper {
    @Select("SELECT * FROM reports ORDER BY create_time DESC, id DESC")
    List<Report> findAll();

    @Select("SELECT * FROM reports WHERE id = #{id}")
    Report findById(Long id);

    @Select("SELECT * FROM reports WHERE phone = #{phone} ORDER BY create_time DESC, id DESC")
    List<Report> findByPhone(String phone);

    @Select("SELECT MAX(id) FROM reports")
    Long findLatestId();

    @Insert("""
            INSERT INTO reports (reporter_name, phone, area_name, location, category, description, status, source, create_time, handler)
            VALUES (#{request.reporterName}, #{request.phone}, #{request.areaName}, #{request.location}, #{request.category},
                    #{request.description}, '待受理', #{source}, #{createTime}, NULL)
            """)
    int insert(@Param("request") CreateReportRequest request, @Param("source") String source, @Param("createTime") String createTime);

    @Update("UPDATE reports SET status = #{status}, handler = #{handler} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("handler") String handler);
}
