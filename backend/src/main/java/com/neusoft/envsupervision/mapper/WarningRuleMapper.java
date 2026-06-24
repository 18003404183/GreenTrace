package com.neusoft.envsupervision.mapper;

import com.neusoft.envsupervision.domain.WarningRule;
import com.neusoft.envsupervision.dto.CreateWarningRuleRequest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface WarningRuleMapper {
    @Select("SELECT * FROM warning_rules ORDER BY id")
    List<WarningRule> findAll();

    @Select("SELECT * FROM warning_rules WHERE enabled = TRUE ORDER BY id")
    List<WarningRule> findEnabled();

    @Insert("""
            INSERT INTO warning_rules (name, metric_name, operator, threshold, level, enabled, advice)
            VALUES (#{request.name}, #{request.metricName}, #{request.operator}, #{request.threshold},
                    #{request.level}, #{enabled}, #{request.advice})
            """)
    int insert(@Param("request") CreateWarningRuleRequest request, @Param("enabled") Boolean enabled);

    @Update("UPDATE warning_rules SET enabled = #{enabled} WHERE id = #{id}")
    int updateEnabled(@Param("id") Long id, @Param("enabled") Boolean enabled);
}
