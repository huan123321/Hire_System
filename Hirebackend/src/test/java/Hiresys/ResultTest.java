package Hiresys;

import Hiresys.dto.Result;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void ok_返回200和success消息() {
        Result<String> result = Result.ok("数据");
        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals("数据", result.getData());
    }

    @Test
    void ok_带自定义消息() {
        Result<String> result = Result.ok("自定义消息", "数据");
        assertEquals(200, result.getCode());
        assertEquals("自定义消息", result.getMessage());
        assertEquals("数据", result.getData());
    }

    @Test
    void ok_无数据() {
        Result<Void> result = Result.ok();
        assertEquals(200, result.getCode());
        assertNull(result.getData());
    }

    @Test
    void fail_带状态码() {
        Result<Void> result = Result.fail(404, "公司不存在");
        assertEquals(404, result.getCode());
        assertEquals("公司不存在", result.getMessage());
        assertNull(result.getData());
    }

    @Test
    void fail_默认400() {
        Result<Void> result = Result.fail("参数错误");
        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMessage());
    }

    @Test
    void 序列化后字段名保持code_message_data() throws Exception {
        Result<Map<String, String>> result = Result.ok(Map.of("k", "v"));
        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(result);
        assertTrue(json.contains("\"code\":200"));
        assertTrue(json.contains("\"message\":\"success\""));
        assertTrue(json.contains("\"data\":{\"k\":\"v\"}"));
    }
}
