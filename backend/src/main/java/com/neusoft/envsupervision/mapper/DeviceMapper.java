package com.neusoft.envsupervision.mapper;

import com.neusoft.envsupervision.domain.Device;
import com.neusoft.envsupervision.dto.CreateDeviceRequest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface DeviceMapper {
    @Select("SELECT * FROM devices ORDER BY id")
    List<Device> findAll();

    @Insert("""
            INSERT INTO devices (name, code, type, area_name, status, last_online, pm25, voc)
            VALUES (#{request.name}, #{request.code}, #{request.type}, #{request.areaName}, #{request.status},
                    #{request.lastOnline}, #{request.pm25}, #{request.voc})
            """)
    int insert(@Param("request") CreateDeviceRequest request);

    @Update("UPDATE devices SET status = #{status}, last_online = #{lastOnline} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("lastOnline") String lastOnline);
}
