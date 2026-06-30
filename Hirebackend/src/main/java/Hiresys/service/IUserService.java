package Hiresys.service;

import Hiresys.entity.User;
import Hiresys.exception.BusinessException;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

public interface IUserService extends IService<User> {

    List<User> selectAll();

    /**
     * 根据 ID 获取用户 (带缓存)
     */
    User getUserById(Integer id);

    /**
     * 保存或更新用户 (清除缓存)
     */
    boolean saveOrUpdateUser(User user);

    /**
     * 删除用户 (清除缓存)
     */
    boolean deleteUserById(Integer id);

    /**
     * 用户注册（包含业务验证）
     */
    Map<String, Object> registerUser(Map<String, Object> requestData, String captchaCode, String sessionId);

    /**
     * 用户登录
     */
    Map<String, Object> login(String username, String password, String captchaCode, String sessionId);
}
