package com.neusoft.envsupervision.mapper;

import com.neusoft.envsupervision.domain.WarningItem;
import com.neusoft.envsupervision.dto.CreateWarningRequest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface WarningMapper {
    @Select("SELECT * FROM warnings ORDER BY created_at DESC, id DESC")
    List<WarningItem> findAll();

    @Select("SELECT COUNT(*) FROM warnings WHERE title = #{title}")
    int countByTitle(String title);

    @Insert("""
            INSERT INTO warnings (title, level, area_name, source_type, status, created_at, advice)
            VALUES (#{request.title}, #{request.level}, #{request.areaName}, #{request.sourceType}, '未处置', #{createdAt}, #{request.advice})
            """)
    int insert(@Param("request") CreateWarningRequest request, @Param("createdAt") String createdAt);

    @Update("UPDATE warnings SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Insert("""
            INSERT INTO warnings (title, level, area_name, source_type, status, created_at, advice)
            VALUES (#{title}, #{level}, #{areaName}, #{sourceType}, '未处置', #{createdAt}, #{advice})
            """)
    int insertAuto(
            @Param("title") String title,
            @Param("level") String level,
            @Param("areaName") String areaName,
            @Param("sourceType") String sourceType,
            @Param("createdAt") String createdAt,
            @Param("advice") String advice
    );
}
