package Hiresys.dto;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;

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

    /**
     * 兼容前端 location 传数组（如 ["中国","北京","北京市"]）的情况，
     * 统一转为逗号分隔的字符串。
     */
    @JsonSetter("location")
    public void setLocationJson(Object value) {
        if (value == null) {
            this.location = null;
        } else if (value instanceof String) {
            this.location = (String) value;
        } else if (value instanceof Collection<?> coll) {
            this.location = String.join(",", coll.stream().map(String::valueOf).toList());
        } else {
            this.location = value.toString();
        }
    }

    /**
     * 兼容前端 gender 传 "male"/"female" 字符串（而非 true/false 布尔）的情况。
     */
    @JsonSetter("gender")
    public void setGenderJson(Object value) {
        if (value == null) {
            this.gender = null;
        } else if (value instanceof Boolean b) {
            this.gender = b;
        } else {
            String s = value.toString().toLowerCase();
            if ("male".equals(s) || "true".equals(s) || "1".equals(s)) {
                this.gender = true;
            } else if ("female".equals(s) || "false".equals(s) || "0".equals(s)) {
                this.gender = false;
            } else {
                this.gender = null;
            }
        }
    }

    // �?注意：以下敏感字段不会包含在 DTO �?    // - password (密码)
    // - lastLoginIp (最后登录IP)
    // - isDeleted (删除标志)
}
