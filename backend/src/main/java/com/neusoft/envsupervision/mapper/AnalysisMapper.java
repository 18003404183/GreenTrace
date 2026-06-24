package com.neusoft.envsupervision.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface AnalysisMapper {
    @Select("SELECT COUNT(*) FROM user_accounts WHERE status = '启用'")
    long activeUsers();

    @Select("SELECT COUNT(*) FROM areas")
    long areas();

    @Select("SELECT COUNT(*) FROM devices")
    long devices();

    @Select("SELECT COUNT(*) FROM devices WHERE status = '在线'")
    long onlineDevices();

    @Select("SELECT COUNT(*) FROM reports")
    long reports();

    @Select("SELECT COUNT(*) FROM warnings WHERE status <> '已处置'")
    long activeWarnings();

    @Select("SELECT category AS name, COUNT(*) AS count_value FROM reports GROUP BY category ORDER BY count_value DESC")
    List<Map<String, Object>> reportCategoryStats();

    @Select("SELECT status AS name, COUNT(*) AS count_value FROM reports GROUP BY status ORDER BY count_value DESC")
    List<Map<String, Object>> reportStatusStats();

    @Select("SELECT area_name AS name, COUNT(*) AS count_value FROM reports GROUP BY area_name ORDER BY count_value DESC")
    List<Map<String, Object>> areaReportStats();
}
