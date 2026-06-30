package Hiresys.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.time.LocalDateTime;
import java.io.Serial;
import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 面试安排�?
 * </p>
 *
 * @author author
 * @since 2026-04-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("interview")
public class Interview implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("application_id")
    private Integer applicationId;

    @TableField("job_id")
    private Integer jobId;

    @TableField("user_id")
    private Integer userId;

    @TableField("company_id")
    private Long companyId;

    @TableField("interview_time")
    private LocalDateTime interviewTime;

    @TableField("interview_type")
    private String interviewType;

    @TableField("location")
    private String location;

    @TableField("meeting_url")
    private String meetingUrl;

    @TableField("interviewer")
    private String interviewer;

    @TableField("interviewer_phone")
    private String interviewerPhone;

    @TableField("status")
    private String status;

    @TableField("feedback")
    private String feedback;

    @TableField("rating")
    private Integer rating;

    @TableField("remark")
    private String remark;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    // ========== 非数据库字段，用于前端展�?==========
    
    @TableField(exist = false)
    private String jobTitle;

    @TableField(exist = false)
    private String companyName;

    @TableField(exist = false)
    private String candidateName;

    @TableField(exist = false)
    private String candidateUsername;
}
