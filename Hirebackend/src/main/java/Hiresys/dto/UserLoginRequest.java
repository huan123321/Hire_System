package Hiresys.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 用户登录请求 DTO
 */
@Data
public class UserLoginRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String username;

    private String password;

    private String captchaCode;

    private String sessionId;
}
