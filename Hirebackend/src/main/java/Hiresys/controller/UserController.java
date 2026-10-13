package Hiresys.controller;

import Hiresys.converter.UserConverter;
import Hiresys.dto.Result;
import Hiresys.dto.UserDTO;
import Hiresys.dto.UserLoginRequest;
import Hiresys.dto.UserRegisterRequest;
import Hiresys.entity.User;
import Hiresys.entity.Company;
import Hiresys.service.IUserService;
import Hiresys.service.ICompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@Tag(name = "用户管理", description = "用户注册、登录、信息查询与更新")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private ICompanyService companyService;

    @Operation(summary = "获取所有用户列表")
    @GetMapping("/")
    public Result<List<UserDTO>> getAllUsers() {
        List<User> users = userService.selectAll();
        return Result.ok(UserConverter.toDTOList(users));
    }

    @Operation(summary = "根据ID获取用户")
    @GetMapping("/{id}")
    public Result<UserDTO> getUserById(@PathVariable Integer id) {
        User user = userService.getUserById(id);
        return Result.ok(UserConverter.toDTO(user));
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/current")
    public Result<UserDTO> getCurrentUser(HttpServletRequest request) {
        // 从认证上下文获取当前用户 ID（登录时签发 token，拦截器已校验）
        Integer userId = (Integer) request.getAttribute(Hiresys.auth.AuthInterceptor.ATTR_USER_ID);
        if (userId == null) {
            return Result.fail(401, "未登录或登录已过期");
        }
        log.info("获取当前用户, id={}", userId);
        User user = userService.getById(userId);

        if (user == null) {
            return Result.fail(404, "用户不存在");
        }

        log.info("找到用户: {}", user.getUsername());

        if (user.getCompanyId() != null) {
            Company company = companyService.getById(user.getCompanyId());
            if (company != null) {
                user.setCompanyName(company.getName());
            } else {
                log.info("未找到公司 ID={}", user.getCompanyId());
            }
        }

        return Result.ok(UserConverter.toDTO(user));
    }

    @Operation(summary = "用户注册")
    @PostMapping("/")
    public Result<?> createUser(@RequestBody(required = false) UserRegisterRequest registerRequest,
                                @RequestParam(required = false) String captchaCode,
                                @RequestParam(required = false) String sessionId) {
        if (registerRequest == null) {
            registerRequest = new UserRegisterRequest();
        }
        // 兼容前端 query 传验证码
        if (captchaCode != null) {
            registerRequest.setCaptchaCode(captchaCode);
        }
        if (sessionId != null) {
            registerRequest.setSessionId(sessionId);
        }
        Map<String, Object> result = userService.registerUser(
                convertRegisterRequestToMap(registerRequest),
                registerRequest.getCaptchaCode(),
                registerRequest.getSessionId()
        );
        return Result.ok((String) result.get("message"), result.get("user"));
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public Result<UserDTO> updateUser(@PathVariable Integer id,
                                      @RequestBody UserDTO userDTO,
                                      HttpServletRequest request) {
        // 只能更新自己的信息（从 token 取身份，不信任路径/请求体里的 id）
        Integer currentUserId = (Integer) request.getAttribute(Hiresys.auth.AuthInterceptor.ATTR_USER_ID);
        if (currentUserId == null || !currentUserId.equals(id)) {
            return Result.fail(403, "无权修改该用户信息");
        }

        if (userDTO.getId() == null) {
            userDTO.setId(id);
        } else if (!userDTO.getId().equals(id)) {
            return Result.fail("路径 ID 与用户 ID 不一致");
        }

        // 防越权提权：普通资料更新接口不允许修改敏感字段（角色/状态/所属公司由管理员或注册流程控制）
        userDTO.setRole(null);
        userDTO.setStatus(null);
        userDTO.setCompanyId(null);

        User user = UserConverter.toEntity(userDTO);
        boolean updated = userService.saveOrUpdateUser(user);

        if (updated) {
            User updatedUser = userService.getById(user.getId());
            return Result.ok("用户信息更新成功", UserConverter.toDTO(updatedUser));
        }
        return Result.fail("用户信息更新失败");
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<?> login(@RequestBody(required = false) UserLoginRequest loginRequest,
                           @RequestParam(required = false) String captchaCode,
                           @RequestParam(required = false) String sessionId) {
        if (loginRequest == null) {
            loginRequest = new UserLoginRequest();
        }
        // 兼容前端 query 传验证码（前端在 URL 上拼 captchaCode/sessionId）
        if (captchaCode != null) {
            loginRequest.setCaptchaCode(captchaCode);
        }
        if (sessionId != null) {
            loginRequest.setSessionId(sessionId);
        }
        Map<String, Object> result = userService.login(
                loginRequest.getUsername(),
                loginRequest.getPassword(),
                loginRequest.getCaptchaCode(),
                loginRequest.getSessionId()
        );
        boolean success = (Boolean) result.get("success");
        if (success) {
            // 返回 { user, token }，前端保存 user 到 sessionStorage、token 用于后续请求鉴权
            Map<String, Object> data = new java.util.HashMap<>();
            data.put("user", result.get("user"));
            data.put("token", result.get("token"));
            return Result.ok((String) result.get("message"), data);
        }
        return Result.fail((String) result.get("message"));
    }

    private Map<String, Object> convertRegisterRequestToMap(UserRegisterRequest request) {
        Map<String, Object> map = new java.util.HashMap<>();
        map.put("username", request.getUsername());
        map.put("password", request.getPassword());
        map.put("email", request.getEmail());
        map.put("phone", request.getPhone());
        map.put("nickname", request.getNickname());
        map.put("role", request.getRole());
        map.put("companyId", request.getCompanyId());
        return map;
    }
}
