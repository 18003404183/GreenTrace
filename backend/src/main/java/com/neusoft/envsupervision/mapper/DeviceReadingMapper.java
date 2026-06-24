package com.neusoft.envsupervision.mapper;

import com.neusoft.envsupervision.domain.DeviceReading;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DeviceReadingMapper {
    @Select("SELECT * FROM device_readings WHERE device_id = #{deviceId} ORDER BY recorded_at, id")
    List<DeviceReading> findByDeviceId(Long deviceId);

    @Select("SELECT * FROM device_readings ORDER BY recorded_at DESC, id DESC")
    List<DeviceReading> findAll();
}
