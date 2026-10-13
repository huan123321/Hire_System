package Hiresys.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotJobServiceImplTest {

    private static final String KEY = "hot:jobs";

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOps;

    private HotJobServiceImpl hotJobService;

    @BeforeEach
    void setUp() throws Exception {
        hotJobService = new HotJobServiceImpl();
        Field f = HotJobServiceImpl.class.getDeclaredField("redisTemplate");
        f.setAccessible(true);
        f.set(hotJobService, redisTemplate);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOps);
    }

    @Test
    void recordView_浏览量加1() {
        hotJobService.recordView(1L);
        verify(zSetOps).incrementScore(KEY, "1", 1.0);
    }

    @Test
    void recordApply_投递量加10() {
        hotJobService.recordApply(1L);
        verify(zSetOps).incrementScore(KEY, "1", 10.0);
    }

    @Test
    void getHotJobIds_按分数倒序返回TopN() {
        when(zSetOps.reverseRange(KEY, 0, 9)).thenReturn(Set.of("5", "3", "8"));
        List<Long> ids = hotJobService.getHotJobIds(10);
        assertEquals(3, ids.size());
        assertTrue(ids.containsAll(List.of(5L, 3L, 8L)));
    }

    @Test
    void getScore_返回对应分数() {
        when(zSetOps.score(KEY, "1")).thenReturn(42.0);
        assertEquals(42.0, hotJobService.getScore(1L));
    }

    @Test
    void Redis不可用_recordView降级内存不抛异常() {
        when(zSetOps.incrementScore(anyString(), anyString(), anyDouble()))
                .thenThrow(new RuntimeException("redis down"));

        // 不应抛异常（降级到内存 Map）
        assertDoesNotThrow(() -> hotJobService.recordView(1L));
        assertDoesNotThrow(() -> hotJobService.recordApply(1L));
    }

    @Test
    void Redis不可用_getHotJobIds从内存降级返回() {
        when(zSetOps.incrementScore(anyString(), anyString(), anyDouble()))
                .thenThrow(new RuntimeException("redis down"));
        hotJobService.recordView(1L);
        hotJobService.recordView(2L);
        hotJobService.recordApply(1L); // 1号分数 = 11

        List<Long> ids = hotJobService.getHotJobIds(10);

        // 按分数倒序：1号(11) 在 2号(1) 之前
        assertEquals(2, ids.size());
        assertEquals(1L, ids.get(0));
        assertEquals(2L, ids.get(1));
    }

    @Test
    void Redis读取失败_getHotJobIds返回内存兜底() {
        when(zSetOps.incrementScore(anyString(), anyString(), anyDouble()))
                .thenThrow(new RuntimeException("redis down"));
        hotJobService.recordView(9L);

        List<Long> ids = hotJobService.getHotJobIds(5);
        assertEquals(1, ids.size());
        assertEquals(9L, ids.get(0));
    }
}
