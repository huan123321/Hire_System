package Hiresys.entity;

import com.baomidou.mybatisplus.annotation.TableName;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.Serial;
import java.io.Serializable;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * Users and global privileges
 * </p>
 *
 * @author author
 * @since 2025-09-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * id
     */
    private Integer id;
    /**
     * 用户�?
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机�?
     */
    private String phone;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别�?-未知�?-男，2-�?
     */
    private Boolean gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 状态：0-禁用�?-正常
     */
    private Boolean status;

    /**
     * 最后登录时�?
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 删除标志�?-未删除，1-已删�?
     */
    private Boolean isDeleted;
    
    /**
     * 用户角色�?-求职者，1-HR�?-管理�?
     */
    private Integer role;
    
    /**
     * 个人简�?
     */
    private String bio;
    
    /**
     * 所在地
     */
    private String location;
    
    /**
     * 所属公�?ID (HR 用户专用)
     */
    private Long companyId;
    
    /**
     * 所属公司名�?(HR 用户专用，不存储到数据库)
     */
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private String companyName;
}