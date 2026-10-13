package Hiresys.service.impl;

import Hiresys.auth.TokenService;
import Hiresys.entity.Company;
import Hiresys.entity.User;
import Hiresys.exception.BusinessException;
import Hiresys.mapper.CompanyMapper;
import Hiresys.mapper.UserMapper;
import Hiresys.service.CaptchaService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private CompanyMapper companyMapper;

    @Mock
    private CaptchaService captchaService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() throws Exception {
        Field baseMapperField = ServiceImpl.class.getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(userService, userMapper);
    }

    private Map<String, Object> registerData() {
        Map<String, Object> data = new HashMap<>();
        data.put("username", "zhangsan");
        data.put("password", "123456");
        data.put("role", 0);
        return data;
    }

    // ==================== 注册 ====================

    @Test
    void registerUser_验证码为空抛业务异常() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.registerUser(registerData(), "", "sid1"));
        assertEquals("请输入验证码", ex.getMessage());
    }

    @Test
    void registerUser_用户名已存在抛业务异常() {
        User existing = new User();
        existing.setUsername("zhangsan");
        when(userMapper.selectOne(any())).thenReturn(existing);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.registerUser(registerData(), "AbCd", "sid1"));
        assertEquals("用户名已存在", ex.getMessage());
    }

    @Test
    void registerUser_成功注册求职者() {
        // 第一次 selectOne：查重返回 null；第二次：注册后查回返回用户
        User registered = new User();
        registered.setUsername("zhangsan");
        registered.setRole(0);
        when(userMapper.selectOne(any())).thenReturn(null, registered);
        when(userMapper.insert(any(User.class))).thenReturn(1);
        when(passwordEncoder.encode("123456")).thenReturn("$2a$10$hash");

        Map<String, Object> result = userService.registerUser(registerData(), "AbCd", "sid1");

        assertEquals(Boolean.TRUE, result.get("success"));
        assertNotNull(result.get("user"));
        verify(captchaService).verifyCaptcha("sid1", "AbCd");
        verify(passwordEncoder).encode("123456");
    }

    @Test
    void registerUser_HR注册时创建公司并关联() {
        User registered = new User();
        registered.setUsername("zhangsan");
        registered.setRole(1);
        when(userMapper.selectOne(any())).thenReturn(null, registered);
        when(userMapper.insert(any(User.class))).thenReturn(1);
        when(companyMapper.insert(any(Company.class))).thenReturn(1);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");

        Map<String, Object> data = registerData();
        data.put("role", 1);
        data.put("companyName", "测试公司");
        data.put("creditCode", "91110000XXX");

        Map<String, Object> result = userService.registerUser(data, "AbCd", "sid1");

        assertEquals(Boolean.TRUE, result.get("success"));
        verify(companyMapper).insert(any(Company.class));
        User user = (User) result.get("user");
        assertNotNull(user);
        assertEquals(1, user.getRole().intValue());
    }

    @Test
    void registerUser_HR注册缺少企业名称抛异常() {
        when(userMapper.selectOne(any())).thenReturn(null);

        Map<String, Object> data = registerData();
        data.put("role", 1);
        data.put("creditCode", "91110000XXX");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.registerUser(data, "AbCd", "sid1"));
        assertEquals("企业名称不能为空", ex.getMessage());
    }

    // ==================== 登录 ====================

    @Test
    void login_用户名密码为空返回失败() {
        Map<String, Object> result = userService.login("", "", "AbCd", "sid1");
        assertEquals(Boolean.FALSE, result.get("success"));
        assertEquals("用户名和密码不能为空", result.get("message"));
    }

    @Test
    void login_验证码为空返回失败() {
        Map<String, Object> result = userService.login("zhangsan", "123456", null, null);
        assertEquals(Boolean.FALSE, result.get("success"));
        assertEquals("请输入验证码", result.get("message"));
    }

    @Test
    void login_验证码校验失败返回失败() {
        doThrow(new BusinessException("验证码错误"))
                .when(captchaService).verifyCaptcha("sid1", "wrong");
        Map<String, Object> result = userService.login("zhangsan", "123456", "wrong", "sid1");
        assertEquals(Boolean.FALSE, result.get("success"));
        assertEquals("验证码错误", result.get("message"));
    }

    @Test
    void login_用户不存在返回失败() {
        when(userMapper.selectOne(any())).thenReturn(null);
        Map<String, Object> result = userService.login("nobody", "123456", "AbCd", "sid1");
        assertEquals(Boolean.FALSE, result.get("success"));
        assertEquals("用户名或密码错误", result.get("message"));
    }

    @Test
    void login_BCrypt密码匹配成功() {
        User dbUser = new User();
        dbUser.setUsername("zhangsan");
        dbUser.setPassword("$2a$10$bcrypt");
        when(userMapper.selectOne(any())).thenReturn(dbUser);
        when(passwordEncoder.matches("123456", "$2a$10$bcrypt")).thenReturn(true);

        Map<String, Object> result = userService.login("zhangsan", "123456", "AbCd", "sid1");

        assertEquals(Boolean.TRUE, result.get("success"));
        assertEquals("登录成功", result.get("message"));
        User user = (User) result.get("user");
        assertNotNull(user);
        assertNull(user.getPassword(), "登录响应必须清除密码");
    }

    @Test
    void login_密码错误返回失败() {
        User dbUser = new User();
        dbUser.setUsername("zhangsan");
        dbUser.setPassword("$2a$10$bcrypt");
        when(userMapper.selectOne(any())).thenReturn(dbUser);
        when(passwordEncoder.matches("wrong", "$2a$10$bcrypt")).thenReturn(false);

        Map<String, Object> result = userService.login("zhangsan", "wrong", "AbCd", "sid1");

        assertEquals(Boolean.FALSE, result.get("success"));
        assertEquals("用户名或密码错误", result.get("message"));
    }

    @Test
    void login_旧明文密码自动升级为BCrypt() {
        User dbUser = new User();
        dbUser.setUsername("zhangsan");
        dbUser.setPassword("plain123");
        when(userMapper.selectOne(any())).thenReturn(dbUser);
        when(passwordEncoder.matches("plain123", "plain123")).thenReturn(false);
        when(passwordEncoder.encode("plain123")).thenReturn("$2a$10$new");
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        Map<String, Object> result = userService.login("zhangsan", "plain123", "AbCd", "sid1");

        assertEquals(Boolean.TRUE, result.get("success"));
        verify(passwordEncoder).encode("plain123");
        verify(userMapper).updateById(any(User.class));
    }
}
