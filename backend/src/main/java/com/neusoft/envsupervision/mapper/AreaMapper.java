package com.neusoft.envsupervision.mapper;

import com.neusoft.envsupervision.domain.Area;
import com.neusoft.envsupervision.dto.CreateAreaRequest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AreaMapper {
    @Select("SELECT * FROM areas ORDER BY parent_id NULLS FIRST, id")
    List<Area> findAll();

    @Insert("""
            INSERT INTO areas (name, code, parent_id, manager, risk_level, address)
            VALUES (#{request.name}, #{request.code}, #{request.parentId}, #{request.manager}, #{request.riskLevel}, #{request.address})
            """)
    int insert(@Param("request") CreateAreaRequest request);
}
