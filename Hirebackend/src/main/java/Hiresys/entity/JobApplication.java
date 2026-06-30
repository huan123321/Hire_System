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
 * 职位申请记录�?
 * </p>
 *
 * @author author
 * @since 2025-09-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("job_application")
public class JobApplication implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("job_id")
    private Integer jobId;

    @TableField("user_id")
    private Integer userId;

    @TableField("status")
    private String status;

    @TableField("feedback")
    private String feedback;

    @TableField("apply_time")
    private LocalDateTime applyTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("resume_id")
    private Integer resumeId;
}
