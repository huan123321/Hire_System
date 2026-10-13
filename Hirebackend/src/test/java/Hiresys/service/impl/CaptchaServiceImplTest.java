package Hiresys.service.impl;

import Hiresys.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CaptchaServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    private CaptchaServiceImpl captchaService;

    @BeforeEach
    void setUp() throws Exception {
        captchaService = new CaptchaServiceImpl();
        // @Autowired(required=false) 字段需要反射注入 mock
        Field redisField = CaptchaServiceImpl.class.getDeclaredField("redisTemplate");
        redisField.setAccessible(true);
        redisField.set(captchaService, redisTemplate);

        // 重置静态状态，避免测试间污染（captchaStore 是 static final，只能 clear 不能替换）
        setStatic("redisAvailable", true);
        Field storeField = CaptchaServiceImpl.class.getDeclaredField("captchaStore");
        storeField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String> store = (Map<String, String>) storeField.get(null);
        store.clear();
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    private static void setStatic(String fieldName, Object value) throws Exception {
        Field f = CaptchaServiceImpl.class.getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(null, value);
    }

    @Test
    void generateCaptchaCode_生成4位且来自合法字符集() {
        String code = captchaService.generateCaptchaCode();
        assertEquals(4, code.length());
        String legal = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        for (char c : code.toCharArray()) {
            assertTrue(legal.indexOf(c) >= 0, "非法字符: " + c);
        }
    }

    @Test
    void storeCaptcha_写入Redis并设置5分钟TTL() {
        captchaService.storeCaptcha("sid1", "AbCd");
        verify(valueOps).set("captcha:sid1", "AbCd", 5 * 60, TimeUnit.SECONDS);
    }

    @Test
    void verifyCaptcha_校验成功并删除验证码() throws Exception {
        when(valueOps.get("captcha:sid1")).thenReturn("AbCd");
        when(redisTemplate.delete("captcha:sid1")).thenReturn(true);

        captchaService.verifyCaptcha("sid1", "abcd");

        verify(redisTemplate).delete("captcha:sid1");
    }

    @Test
    void verifyCaptcha_大小写不敏感() throws Exception {
        when(valueOps.get("captcha:sid1")).thenReturn("AbCd");
        captchaService.verifyCaptcha("sid1", "ABCD");
        captchaService.verifyCaptcha("sid1", "abcd");
    }

    @Test
    void verifyCaptcha_验证码错误抛业务异常() {
        when(valueOps.get("captcha:sid1")).thenReturn("AbCd");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> captchaService.verifyCaptcha("sid1", "XxXx"));
        assertEquals("验证码错误", ex.getMessage());
    }

    @Test
    void verifyCaptcha_验证码过期不存在抛业务异常() {
        when(valueOps.get("captcha:sid1")).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> captchaService.verifyCaptcha("sid1", "AbCd"));
        assertEquals("验证码已过期或不存在", ex.getMessage());
    }

    @Test
    void verifyCaptcha_参数为空抛业务异常() {
        BusinessException ex1 = assertThrows(BusinessException.class,
                () -> captchaService.verifyCaptcha("", "AbCd"));
        assertEquals("验证码参数不能为空", ex1.getMessage());

        BusinessException ex2 = assertThrows(BusinessException.class,
                () -> captchaService.verifyCaptcha("sid1", ""));
        assertEquals("验证码参数不能为空", ex2.getMessage());
    }

    @Test
    void Redis不可用_降级内存存储后仍可校验() throws Exception {
        // Redis 操作抛异常 → 降级内存
        when(redisTemplate.opsForValue()).thenThrow(new RuntimeException("connection refused"));
        setStatic("redisAvailable", true);

        captchaService.storeCaptcha("sid_mem", "wXyZ");

        // 校验时 redisAvailable 已为 false，从内存读取
        captchaService.verifyCaptcha("sid_mem", "wxyz");
    }

    @Test
    void Redis读取失败_回退内存存储() throws Exception {
        setStatic("redisAvailable", false);
        Field storeField = CaptchaServiceImpl.class.getDeclaredField("captchaStore");
        storeField.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, String> store = (Map<String, String>) storeField.get(null);
        store.put("sid_mem2", "K9mN");

        captchaService.verifyCaptcha("sid_mem2", "K9mN");
    }
}
