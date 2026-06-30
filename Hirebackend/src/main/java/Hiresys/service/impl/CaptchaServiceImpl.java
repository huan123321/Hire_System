package Hiresys.service.impl;

import Hiresys.exception.BusinessException;
import Hiresys.service.CaptchaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CaptchaServiceImpl implements CaptchaService {

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    private static final Map<String, String> captchaStore = new ConcurrentHashMap<>();
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";
    private static final long CAPTCHA_EXPIRE_TIME = 5 * 60;
    private static volatile boolean redisAvailable = true;

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

    @Override
    public void storeCaptcha(String sessionId, String code) {
        if (redisAvailable && redisTemplate != null) {
            try {
                String redisKey = CAPTCHA_KEY_PREFIX + sessionId;
                redisTemplate.opsForValue().set(redisKey, code, CAPTCHA_EXPIRE_TIME, TimeUnit.SECONDS);
                log.info("验证码已存储到 Redis: {}", redisKey);
            } catch (Exception e) {
                log.warn("Redis 存储失败，降级使用内存存储: {}", e.getMessage());
                redisAvailable = false;
                captchaStore.put(sessionId, code);
            }
        } else {
            captchaStore.put(sessionId, code);
            log.info("验证码已存储到内存: {}", sessionId);
        }
    }

    @Override
    public void verifyCaptcha(String sessionId, String code) {
        if (sessionId == null || sessionId.isEmpty() || code == null || code.isEmpty()) {
            throw new BusinessException("验证码参数不能为空");
        }

        String captchaCode = null;

        if (redisAvailable && redisTemplate != null) {
            try {
                String redisKey = CAPTCHA_KEY_PREFIX + sessionId;
                captchaCode = redisTemplate.opsForValue().get(redisKey);
                if (captchaCode != null) {
                    log.info("从 Redis 获取验证码成功: {}", redisKey);
                }
            } catch (Exception e) {
                log.warn("Redis 获取失败，降级使用内存存储: {}", e.getMessage());
                redisAvailable = false;
            }
        }

        if (captchaCode == null) {
            captchaCode = captchaStore.get(sessionId);
            if (captchaCode != null) {
                log.info("从内存获取验证码成功: {}", sessionId);
            }
        }

        if (captchaCode == null) {
            throw new BusinessException("验证码已过期或不存在");
        }

        if (!captchaCode.equalsIgnoreCase(code)) {
            log.warn("验证码错误 - 用户输入: {}, 正确验证码: {}", code, captchaCode);
            throw new BusinessException("验证码错误");
        }

        // 验证成功后删除验证码
        if (redisAvailable && redisTemplate != null) {
            try {
                redisTemplate.delete(CAPTCHA_KEY_PREFIX + sessionId);
            } catch (Exception e) {
                log.warn("删除 Redis 验证码失败: {}", e.getMessage());
            }
        }
        captchaStore.remove(sessionId);
    }

    @Override
    public String generateCaptchaCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
