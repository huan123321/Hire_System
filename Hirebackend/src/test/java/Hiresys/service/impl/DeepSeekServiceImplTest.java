package Hiresys.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeepSeekServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    private DeepSeekServiceImpl deepSeekService;

    @BeforeEach
    void setUp() throws Exception {
        deepSeekService = new DeepSeekServiceImpl();
        Field keyField = DeepSeekServiceImpl.class.getDeclaredField("apiKey");
        keyField.setAccessible(true);
        keyField.set(deepSeekService, "");
    }

    private void injectMockRestTemplate() throws Exception {
        Field f = DeepSeekServiceImpl.class.getDeclaredField("restTemplate");
        f.setAccessible(true);
        f.set(deepSeekService, restTemplate);
    }

    @Test
    void apiKey未配置_返回错误JSON且不抛异常() {
        String reply = deepSeekService.chat("你好");
        assertTrue(reply.contains("AI 服务未配置"));
    }

    @Test
    void chat_正常返回模型回复() throws Exception {
        injectMockRestTemplate();
        setApiKey("sk-test");

        Map<String, Object> message = Map.of("role", "assistant", "content", "你好，我是招聘助手");
        Map<String, Object> choice = Map.of("message", message);
        Map<String, Object> body = Map.of("choices", List.of(choice));
        ResponseEntity<Map> response = new ResponseEntity<>(body, HttpStatus.OK);
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class))).thenReturn(response);

        String reply = deepSeekService.chat("你好");

        assertEquals("你好，我是招聘助手", reply);
    }

    @Test
    void API调用异常_返回错误JSON不抛异常() throws Exception {
        injectMockRestTemplate();
        setApiKey("sk-test");

        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .thenThrow(new RuntimeException("network down"));

        String reply = deepSeekService.analyzeResume("简历文本", "Java开发");
        assertTrue(reply.contains("error") || reply.contains("失败"));
    }

    @Test
    void generateInterviewQuestions_提示词包含职位信息() throws Exception {
        injectMockRestTemplate();
        setApiKey("sk-test");

        Map<String, Object> message = Map.of("role", "assistant", "content", "ok");
        Map<String, Object> choice = Map.of("message", message);
        Map<String, Object> body = Map.of("choices", List.of(choice));
        when(restTemplate.postForEntity(anyString(), any(), eq(Map.class))).thenReturn(
                new ResponseEntity<>(body, HttpStatus.OK));

        deepSeekService.generateInterviewQuestions("Java工程师", "负责后端开发", "熟悉Spring");

        // 捕获实际请求体，验证提示词构造
        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<Map<String, Object>>> captor =
                ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(anyString(), captor.capture(), eq(Map.class));

        Map<String, Object> requestBody = captor.getValue().getBody();
        assertNotNull(requestBody);
        assertEquals("deepseek-chat", requestBody.get("model"));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> messages = (List<Map<String, Object>>) requestBody.get("messages");
        assertEquals(2, messages.size());
        String userPrompt = (String) messages.get(1).get("content");
        assertTrue(userPrompt.contains("Java工程师"));
        assertTrue(userPrompt.contains("熟悉Spring"));
    }

    private void setApiKey(String key) throws Exception {
        Field keyField = DeepSeekServiceImpl.class.getDeclaredField("apiKey");
        keyField.setAccessible(true);
        keyField.set(deepSeekService, key);
    }
}
