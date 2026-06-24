package com.neusoft.envsupervision.mapper;

import com.neusoft.envsupervision.domain.Attachment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AttachmentMapper {
    @Select("SELECT * FROM attachments WHERE report_id = #{reportId} ORDER BY uploaded_at DESC, id DESC")
    List<Attachment> findByReportId(Long reportId);

    @Insert("""
            INSERT INTO attachments (report_id, record_id, file_name, file_path, content_type, usage_type, uploaded_by, uploaded_at)
            VALUES (#{reportId}, #{recordId}, #{fileName}, #{filePath}, #{contentType}, #{usageType}, #{uploadedBy}, #{uploadedAt})
            """)
    int insert(
            @Param("reportId") Long reportId,
            @Param("recordId") Long recordId,
            @Param("fileName") String fileName,
            @Param("filePath") String filePath,
            @Param("contentType") String contentType,
            @Param("usageType") String usageType,
            @Param("uploadedBy") String uploadedBy,
            @Param("uploadedAt") String uploadedAt
    );
}
