package Hiresys.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户信息 DTO（用于响应，不包含敏感信息）
 */
@Data
public class UserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer id;

    private String username;

    private String email;

    private String phone;

    private String nickname;

    private String realName;

    private String avatar;

    private Boolean gender;

    private LocalDate birthday;

    private Boolean status;

    private LocalDateTime lastLoginTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer role;

    private String bio;

    private String location;

    private Long companyId;

    private String companyName;

    // �?注意：以下敏感字段不会包含在 DTO �?    // - password (密码)
    // - lastLoginIp (最后登录IP)
    // - isDeleted (删除标志)
}
