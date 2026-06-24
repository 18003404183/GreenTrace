package com.neusoft.envsupervision.mapper;

import com.neusoft.envsupervision.domain.MapPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MapPointMapper {
    @Select("SELECT * FROM map_points ORDER BY id")
    List<MapPoint> findAll();
}
