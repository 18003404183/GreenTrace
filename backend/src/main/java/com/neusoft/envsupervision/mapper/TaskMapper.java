package com.neusoft.envsupervision.mapper;

import com.neusoft.envsupervision.domain.SupervisionTask;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface TaskMapper {
    @Select("SELECT * FROM supervision_tasks ORDER BY created_at DESC, id DESC")
    List<SupervisionTask> findAll();

    @Select("SELECT * FROM supervision_tasks WHERE report_id = #{reportId} ORDER BY created_at DESC, id DESC")
    List<SupervisionTask> findByReportId(Long reportId);

    @Select("SELECT * FROM supervision_tasks WHERE status <> '已完成' AND status <> '已逾期' AND deadline < #{now} ORDER BY deadline, id")
    List<SupervisionTask> findOverdue(String now);

    @Insert("""
            INSERT INTO supervision_tasks (task_no, report_id, title, assignee, deadline, priority, status, created_at, completed_at)
            VALUES (#{taskNo}, #{reportId}, #{title}, #{assignee}, #{deadline}, #{priority}, '待处理', #{createdAt}, NULL)
            """)
    int insert(
            @Param("taskNo") String taskNo,
            @Param("reportId") Long reportId,
            @Param("title") String title,
            @Param("assignee") String assignee,
            @Param("deadline") String deadline,
            @Param("priority") String priority,
            @Param("createdAt") String createdAt
    );

    @Update("UPDATE supervision_tasks SET status = #{status}, completed_at = #{completedAt} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("completedAt") String completedAt);
}
