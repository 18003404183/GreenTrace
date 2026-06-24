package com.neusoft.envsupervision.mapper;

import com.neusoft.envsupervision.domain.NotificationItem;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NotificationMapper {
    @Select("SELECT * FROM notifications ORDER BY created_at DESC, id DESC")
    List<NotificationItem> findAll();

    @Insert("""
            INSERT INTO notifications (title, content, type, read_flag, created_at)
            VALUES (#{title}, #{content}, #{type}, FALSE, #{createdAt})
            """)
    int insert(
            @Param("title") String title,
            @Param("content") String content,
            @Param("type") String type,
            @Param("createdAt") String createdAt
    );

    @Update("UPDATE notifications SET read_flag = TRUE WHERE id = #{id}")
    int markRead(Long id);
}
