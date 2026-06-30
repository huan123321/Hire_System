package Hiresys.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 用户注册请求 DTO
 */
@Data
public class UserRegisterRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String username;

    private String password;

    private String email;

    private String phone;

    private String nickname;

    private Integer role;

    private Long companyId;

    private String captchaCode;

    private String sessionId;
}
