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
    public Result<UserDTO> getCurrentUser(@RequestParam String id) {
        log.info("获取当前用户, id={}", id);
        Integer userId = Integer.parseInt(id);
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
    public Result<?> createUser(@RequestBody UserRegisterRequest registerRequest) {
        Map<String, Object> result = userService.registerUser(
                convertRegisterRequestToMap(registerRequest),
                registerRequest.getCaptchaCode(),
                registerRequest.getSessionId()
        );
        return Result.ok((String) result.get("message"), result.get("user"));
    }

    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public Result<UserDTO> updateUser(@PathVariable Integer id, @RequestBody UserDTO userDTO) {
        if (userDTO.getId() == null) {
            userDTO.setId(id);
        } else if (!userDTO.getId().equals(id)) {
            return Result.fail("路径 ID 与用户 ID 不一致");
        }

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
    public Result<?> login(@RequestBody UserLoginRequest loginRequest) {
        Map<String, Object> result = userService.login(
                loginRequest.getUsername(),
                loginRequest.getPassword(),
                loginRequest.getCaptchaCode(),
                loginRequest.getSessionId()
        );
        boolean success = (Boolean) result.get("success");
        if (success) {
            return Result.ok((String) result.get("message"), result.get("user"));
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
