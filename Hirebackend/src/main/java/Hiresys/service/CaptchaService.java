package Hiresys.service;

/**
 * 验证码服务接口
 */
public interface CaptchaService {

    /**
     * 存储验证码
     * @param sessionId 会话ID
     * @param code 验证码
     */
    void storeCaptcha(String sessionId, String code);

    /**
     * 验证验证码
     * @param sessionId 会话ID
     * @param code 用户输入的验证码
     * @throws Hiresys.exception.BusinessException 验证失败时抛出
     */
    void verifyCaptcha(String sessionId, String code);

    /**
     * 生成随机验证码
     * @return 4位验证码字符串
     */
    String generateCaptchaCode();
}
