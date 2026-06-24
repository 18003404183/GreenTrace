package com.neusoft.envsupervision.mapper;

import com.neusoft.envsupervision.domain.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnnouncementMapper {
    @Select("SELECT * FROM announcements ORDER BY published_at DESC, id DESC")
    List<Announcement> findAll();
}
