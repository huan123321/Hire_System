package Hiresys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

import java.time.LocalDateTime;

/**
 * <p>
 * 公司浏览记录�?
 * </p>
 *
 * @author author
 * @since 2025-04-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("company_view_log")
public class CompanyViewLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("company_id")
    private Long companyId;

    @TableField("user_id")
    private Integer userId;

    @TableField("ip_address")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String ipAddress;

    @TableField("view_time")
    private LocalDateTime viewTime;

}
