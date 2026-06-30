package Hiresys.service.impl;

import Hiresys.entity.Company;
import Hiresys.entity.User;
import Hiresys.exception.BusinessException;
import Hiresys.mapper.CompanyMapper;
import Hiresys.mapper.UserMapper;
import Hiresys.service.CaptchaService;
import Hiresys.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<User> selectAll() {
        return this.list();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "user", key = "#user.id", beforeInvocation = false)
    public boolean saveOrUpdateUser(User user) {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }

        user.setUpdateTime(LocalDateTime.now());

        if (user.getId() == null) {
            user.setCreateTime(LocalDateTime.now());
            user.setStatus(true);
            user.setIsDeleted(false);
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            return this.save(user);
        } else {
            User existingUser = this.getById(user.getId());
            if (existingUser == null) {
                throw new BusinessException("用户不存在");
            }

            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            } else {
                user.setPassword(null);
            }

            user.setUsername(null);
            return this.updateById(user);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "user", key = "#id")
    public boolean deleteUserById(Integer id) {
        User user = this.getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setIsDeleted(true);
        user.setUpdateTime(LocalDateTime.now());
        return this.updateById(user);
    }

    @Override
    @Cacheable(value = "user", key = "#id", unless = "#result == null")
    public User getUserById(Integer id) {
        return this.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> registerUser(Map<String, Object> requestData, String captchaCode, String sessionId) {
        Map<String, Object> result = new HashMap<>();

        if (captchaCode == null || captchaCode.isEmpty() || sessionId == null || sessionId.isEmpty()) {
            throw new BusinessException("请输入验证码");
        }
        captchaService.verifyCaptcha(sessionId, captchaCode);

        String username = (String) requestData.get("username");
        String password = (String) requestData.get("password");
        Integer role = requestData.get("role") != null ? ((Number) requestData.get("role")).intValue() : 0;

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new BusinessException("用户名和密码不能为空");
        }

        User existingUser = this.lambdaQuery()
                .eq(User::getUsername, username)
                .one();

        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }

        Long companyId = null;
        if (role != null && role == 1) {
            String companyName = (String) requestData.get("companyName");
            String creditCode = (String) requestData.get("creditCode");
            String legalPerson = (String) requestData.get("legalPerson");
            String registerAddress = (String) requestData.get("registerAddress");
            String contactPhone = (String) requestData.get("contactPhone");

            if (companyName == null || companyName.isEmpty()) {
                throw new BusinessException("企业名称不能为空");
            }
            if (creditCode == null || creditCode.isEmpty()) {
                throw new BusinessException("统一社会信用代码不能为空");
            }

            Company existingCompany = companyMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Company>()
                            .eq(Company::getName, companyName)
            );

            if (existingCompany != null) {
                throw new BusinessException("该公司已注册，请直接登录或联系管理员");
            }

            Company company = new Company();
            company.setName(companyName);
            company.setDescription("由HR " + username + " 于 " + LocalDateTime.now().toString() + " 注册");
            company.setAddress(registerAddress != null ? registerAddress : "未填写");
            company.setPhone(contactPhone != null ? contactPhone : "未填写");
            company.setCreateTime(LocalDateTime.now());
            company.setUpdateTime(LocalDateTime.now());

            boolean companySaved = companyMapper.insert(company) > 0;
            if (!companySaved) {
                throw new BusinessException("公司注册失败");
            }

            companyId = company.getId();
            log.info("创建公司成功: ID={}, 名称={}", companyId, companyName);
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setId(null);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setStatus(true);
        user.setIsDeleted(false);

        if (companyId != null) {
            user.setCompanyId(companyId);
        }

        boolean saved = this.save(user);
        if (!saved) {
            throw new BusinessException("用户注册失败");
        }

        User savedUser = this.lambdaQuery()
                .eq(User::getUsername, username)
                .one();

        // 清除密码，避免返回给前端
        if (savedUser != null) {
            savedUser.setPassword(null);
        }

        result.put("success", true);
        result.put("message", "用户注册成功");
        result.put("user", savedUser);

        return result;
    }

    @Override
    public Map<String, Object> login(String username, String password, String captchaCode, String sessionId) {
        Map<String, Object> result = new HashMap<>();

        if (username == null || username.isEmpty() ||
                password == null || password.isEmpty()) {
            result.put("success", false);
            result.put("message", "用户名和密码不能为空");
            return result;
        }

        if (captchaCode == null || captchaCode.isEmpty() || sessionId == null || sessionId.isEmpty()) {
            result.put("success", false);
            result.put("message", "请输入验证码");
            return result;
        }

        try {
            captchaService.verifyCaptcha(sessionId, captchaCode);
        } catch (BusinessException e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }

        // 只按用户名查询
        User user = this.lambdaQuery()
                .eq(User::getUsername, username)
                .one();

        if (user == null) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }

        // 1. 先尝试 BCrypt 匹配
        if (passwordEncoder.matches(password, user.getPassword())) {
            user.setPassword(null);
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("user", user);
            return result;
        }

        // 2. 兼容旧明文密码：直接比对，匹配则自动升级为 BCrypt
        if (password.equals(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(password));
            user.setUpdateTime(LocalDateTime.now());
            this.updateById(user);
            log.info("用户 {} 密码已自动升级为 BCrypt", username);

            user.setPassword(null);
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("user", user);
            return result;
        }

        result.put("success", false);
        result.put("message", "用户名或密码错误");

        return result;
    }
}
